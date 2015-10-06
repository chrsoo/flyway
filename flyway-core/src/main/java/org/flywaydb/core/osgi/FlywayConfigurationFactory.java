/**
 * Copyright 2010-2015 Axel Fontaine
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.osgi;

import static org.flywaydb.core.Flyway.FLYWAY_DRIVER_PROPERTY;

import java.sql.Driver;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.bundle.BundleScanner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Registers FLyway data source configurations for the Managed Service Factory
 * PID {@value #FLYWAY_FACTORY_PID}.
 */
public class FlywayConfigurationFactory implements ManagedServiceFactory, FlywayFactory {

	private static final Log LOG = LogFactory.getLog(FlywayConfigurationFactory.class);

	public static final String FLYWAY_FACTORY_PID = "org.flywaydb.datasource";

	protected static final String FLYWAY_PROPERTY_PREFIX = "flyway.";

	/**
	 * <p>
	 * The {@value #FLYWAY_DRIVER_NAME_PROPERTY} property is used in the Flyway
	 * data source configuration files to indicate the name of the JDBC driver
	 * to be used for the data source configuration.
	 */
	public static final String FLYWAY_DRIVER_NAME_PROPERTY = FLYWAY_PROPERTY_PREFIX + "driverName";

	/**
	 * <p>
	 * The {@value #FLYWAY_DRIVER_VERSION_PROPERTY} property is used in the
	 * Flyway data source configuration files to indicate the version of the
	 * JDBC driver to be used for the data source configuration.
	 */
	public static final String FLYWAY_DRIVER_VERSION_PROPERTY = FLYWAY_PROPERTY_PREFIX + "driverVersion";

	protected final BundleContext context;

	private final Map<String, Properties> configurations = Collections.synchronizedMap(new HashMap<String, Properties>());

	// -- Constructors

	public FlywayConfigurationFactory(BundleContext context) {
		this.context = context;
	}

	// -- ManagedServiceFactory

	@Override
	public String getName() {
		return "Flyway Configuration Managed Service Factory";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void updated(String pid, Dictionary managedServiceConfig) throws ConfigurationException {

		LOG.debug("Updating Flyway configuration for pid '" + pid + "'");

		String name = getFlywayName(pid);
		LOG.info("Found Flyway configuration for data source '" + name + "'");

		Properties flywayConf = getFlywayConf(managedServiceConfig);
		configurations.put(name, flywayConf);

		LOG.info("Updated Flyway data source configuration '" + name + "'");
	}

	@Override
	public void deleted(String pid) {
		String name = getFlywayName(pid);
		configurations.remove(name);
		LOG.info("Removed the Flyway configuration for data source '" + name + "'");
	}

	// -- FlywayFactory

	private Properties getFlywayConf(Dictionary<?, ?> config) {

		if (config instanceof Properties) {
			return (Properties) config;
		}

		Properties flywayConf = new Properties();
		Enumeration<?> enumeration = config.keys();
		while (enumeration.hasMoreElements()) {
			Object key = enumeration.nextElement();
			Object value = config.get(key);
			flywayConf.put(key, value);
		}

		return flywayConf;
	}

	@Override
	public Flyway create(Bundle bundle, String name, Properties defaultConf)
			throws FlywayException {

		if (!configurations.containsKey(name)) {
			persistDefaultConfig(name, defaultConf);
		}

		Flyway flyway = new Flyway();

		// create a new properties instance so as not to modify the defaultConf
		Properties conf = new Properties(defaultConf);

		// the managed conf overrides the defaultConf
		Properties managedConf = configurations.get(name);
		conf.putAll(managedConf);

		// create the DataSource before we remove the JDBC configuration
		DataSource dataSource = createFlywayDataSource(bundle, conf);

		// make sure that we don't configure the JDBC driver
		conf.remove(Flyway.FLYWAY_DRIVER_PROPERTY);
		conf.remove(Flyway.FLYWAY_URL_PROPERTY);
		conf.remove(Flyway.FLYWAY_USER_PROPERTY);
		conf.remove(Flyway.FLYWAY_PASSWORD_PROPERTY);

		// apply the conf to the Flyway instance
		flyway.configure(conf);

		// set the previously created DataSource
		flyway.setDataSource(dataSource);

		// set the Scanner strategy
		BundleScanner scanner = new BundleScanner(bundle);
		flyway.setScanner(scanner);

		return flyway;
	}

	// -- FlywayDatasourceServiceFactory

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void persistDefaultConfig(String name, Properties defaultConf) {

		String pid = getFlywayPID(name);
		LOG.debug("Creating new Flyway configuration '" + name + "' with pid '" + pid + "' using default values");

		ServiceReference reference = context.getServiceReference(ConfigurationAdmin.class.getName());
		if (reference == null) {
			throw new FlywayException("Could not get ServiceReference for " + ConfigurationAdmin.class);
		}

		try {
			ConfigurationAdmin service = (ConfigurationAdmin) context.getService(reference);
			Configuration flywayConfiguration = service.createFactoryConfiguration(pid);
			flywayConfiguration.update((Dictionary) defaultConf);
		} catch (Exception e) {
			throw new FlywayException("Could not create defaults for '" + name + "' configuration", e);
		} finally {
			context.ungetService(reference);
		}

		LOG.info("Created new Flyway configuration '" + name + "' with pid '" + pid + "' using default values");

	}

	static String getConfigValue(Properties config, String key) {
		String value = (String) config.get(key);
		return value == null
				? null
				: value.trim();
	}

	/**
	 * Create a Flyway DriverDataSource instance optionally looking up the JDBC driver
	 * using the OSGI Compendium DataSourceFactory's
	 * @throws ConfigurationException if there is a configuration problem found when creating the data source
	 */
	private DataSource createFlywayDataSource(Bundle bundle, Properties config) {

		String url = getConfigValue(config, Flyway.FLYWAY_URL_PROPERTY);
		String user = getConfigValue(config, Flyway.FLYWAY_USER_PROPERTY);
		String password = getConfigValue(config, Flyway.FLYWAY_PASSWORD_PROPERTY);

		// FIXME add support for initSql's
		try {
			Driver driver = loadDriver(bundle, config);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Properties jdbcProperties = getJdbcProperties(config);
			return new DriverDataSource(classLoader, driver, url, user, password, jdbcProperties);
		} catch (ConfigurationException e) {
			throw new FlywayException("Could not create flyway datasource", e);
		}
	}

	/**
	 * Load the driver from the given bundle.
	 * @param bundle the bundle whose class loader contains the driver
	 * @param config the configuration properties
	 * @return a JDBC driver instance
	 * @throws ConfigurationException if the driver cannot be loaded
	 */
	@SuppressWarnings("unchecked")
	protected Driver loadDriver(Bundle bundle, Properties config) throws ConfigurationException {

		String driverClassName = config.getProperty(FLYWAY_DRIVER_PROPERTY);
		try {
			Class<Driver> driverClass = (Class<Driver>) bundle.loadClass(driverClassName);
			LOG.info("Loaded the driver '" + driverClassName + "' from the bundle");
			return driverClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(FLYWAY_DRIVER_PROPERTY,

					"Could not load driver '" + driverClassName + "'", e);
		} catch (InstantiationException e) {
			throw new ConfigurationException(FLYWAY_DRIVER_PROPERTY,
					"Could instantiate driver '" + driverClassName + "'; ", e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException(FLYWAY_DRIVER_PROPERTY,
					"Could not access '" + driverClassName + "'; ", e);
		}
	}

	/**
	 * Filter Flyway and OSGI specific configuration from the JDBC properties.
	 * Specifically the <code>service.factoryPid</code> property and all
	 * properties prefixed with {@value #FLYWAY_PROPERTY_PREFIX} and are
	 * removed.
	 *
	 * @return a new Properties instance containing only JDBC configuration
	 *         properties.
	 */
	protected Properties getJdbcProperties(Properties config) {
		Properties properties = new Properties();
		Enumeration<Object> keys = config.keys();
		String key;
		while(keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			if(!key.startsWith(FLYWAY_PROPERTY_PREFIX)) {
				properties.setProperty(key, (String) config.get(key));
			}
		}

		// remove OSGI specific properties added by ConfigurationAdmin when
		// persisting defaults
		properties.remove("service.factoryPid");

		return properties;
	}

	private String getFlywayName(String pid) {
		return pid.substring(pid.lastIndexOf(".") + 1);
	}

	private String getFlywayPID(String name) {
		return FLYWAY_FACTORY_PID + "." + name;
	}

}

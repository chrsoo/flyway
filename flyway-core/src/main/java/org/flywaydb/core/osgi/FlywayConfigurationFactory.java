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
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Registers FLyway configurations for the Managed Service Factory
 * PID {@value #FLYWAY_FACTORY_PID}.
 */
public class FlywayConfigurationFactory implements ManagedServiceFactory, FlywayFactory {

	private static final Log LOG = LogFactory.getLog(FlywayConfigurationFactory.class);

	public static final String FLYWAY_FACTORY_PID = "org.flywaydb.core";

	protected final BundleContext context;

	private final Map<String, FlywayManagedConfiguration> configurations =
			Collections.synchronizedMap(new HashMap<String, FlywayManagedConfiguration>());

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
	public void updated(String pid, Dictionary config) throws ConfigurationException {

		FlywayManagedConfiguration flywayConf = new FlywayManagedConfiguration(config);

		String name = flywayConf.getName();
		if(name == null) {
			LOG.warn("Flyway configuration for pid " + pid
					+ " must define the '"
					+ FlywayManagedConfiguration.FLYWAY_NAME_PROPERTY
					+ "' property");
			return;
		}

		configurations.put(name, flywayConf);
		LOG.info("Updated Flyway configuration '" + name + "' with pid '" + pid + "'");
	}

	@Override
	public void deleted(String pid) {
		String name = getFlywayName(pid);
		configurations.remove(name);
		LOG.info("Removed the Flyway configuration for '" + name + "'");
	}

	// -- FlywayFactory

	@Override
	public Flyway create(Bundle bundle, FlywayBundleConfiguration bundleConfig)
			throws FlywayException {

		LOG.debug("Creating Flyway instance for bundle " + bundle.getBundleId()
				+ "' and '" + bundleConfig.getName() + "'");

		String name = bundleConfig.getName();
		Properties defaultProperties = bundleConfig.loadProperties();

		Flyway flyway = new Flyway();

		// create a new properties instance so as not to modify the defaultConf
		Properties flywayProperties = new Properties();
		flywayProperties.putAll(defaultProperties);

		// the managed conf overrides the defaultConf
		if(configurations.containsKey(name)) {
			FlywayManagedConfiguration managedConfig = configurations.get(name);
			flywayProperties.putAll(managedConfig.getProperties());
		} else {
			LOG.warn("Could not find managed configuration for Flyway '" + name + "'");
		}

		// create the DataSource before we remove the JDBC driver and URL
		DataSource dataSource = createFlywayDataSource(bundle, flywayProperties);
		flyway.setDataSource(dataSource);

		// apply the properties to the Flyway instance
		flyway.configure(flywayProperties);

		// set the Scanner strategy
		BundleScanner scanner = new BundleScanner(bundle);
		flyway.setScanner(scanner);

		return flyway;
	}

	// -- FlywayDatasourceServiceFactory

	/**
	 * Create a Flyway DriverDataSource instance optionally looking up the JDBC driver
	 * using the OSGI Compendium DataSourceFactory's
	 * @throws ConfigurationException if there is a configuration problem found when creating the data source
	 */
	private DataSource createFlywayDataSource(Bundle bundle, Properties flywayProperties) {

		String url = flywayProperties.getProperty(Flyway.FLYWAY_URL_PROPERTY);
		String user = flywayProperties.getProperty(Flyway.FLYWAY_USER_PROPERTY);
		String password = flywayProperties.getProperty(Flyway.FLYWAY_PASSWORD_PROPERTY);

		// FIXME add support for initSql's
		try {
			Driver driver = loadDriver(bundle, flywayProperties);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Properties jdbcProperties = getJdbcProperties(flywayProperties);
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
	protected Driver loadDriver(Bundle bundle, Properties flywayConfig) throws ConfigurationException {

		String driverClassName = flywayConfig.getProperty(FLYWAY_DRIVER_PROPERTY);

		if(driverClassName == null) {
			throw new FlywayException("The '" + FLYWAY_DRIVER_PROPERTY + "' property not defined!");
		}

		try {
			Class<Driver> driverClass = (Class<Driver>) bundle.loadClass(driverClassName);
			Driver driver = driverClass.newInstance();
			LOG.debug("Loaded the driver '" + driverClassName + "' from the bundle");
			return driver;
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
	 * Check all Flyway configurations for a pid that matches
	 * and return the Flyway configuration name.
	 */
	private String getFlywayName(String pid) {
		String configPid;

		for(FlywayManagedConfiguration flywayConf: configurations.values()) {
			configPid = flywayConf.get(Constants.SERVICE_PID);
			if(configPid.equals(pid)) {
				return flywayConf.getName();
			}
		}

		throw new FlywayException("Could not find configuration for pid '" + pid + "'");
	}

	/**
	 * Filter Flyway and OSGI specific configuration from the JDBC properties.
	 * Specifically the <code>service.factoryPid</code> property and all
	 * properties prefixed with {@value Flyway#FLYWAY_PROPERTY_PREFIX} and are
	 * removed.
	 *
	 * @return a new Properties instance containing only JDBC configuration
	 *         properties.
	 */
	public Properties getJdbcProperties(Properties config) {
		Properties properties = new Properties();
		Enumeration<Object> keys = config.keys();
		String key;
		while(keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			// filter all Flyway properties
			if(!key.startsWith(Flyway.FLYWAY_PROPERTY_PREFIX)) {
				properties.setProperty(key, config.getProperty(key));
			}
		}

		// remove OSGI specific properties added by ConfigurationAdmin when
		// persisting defaults
		properties.remove(ConfigurationAdmin.SERVICE_FACTORYPID);
		properties.remove(Constants.SERVICE_PID);
		properties.remove(FlywayManagedConfiguration.FELIX_FILEINSTALL_FILENAME_PROPERTY);

		return properties;
	}

}

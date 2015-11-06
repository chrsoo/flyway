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
import java.util.Enumeration;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;

public class BundleClassLoaderDataSourceFactory implements DataSourceFactoryStrategy {

	private static final Log LOG = LogFactory
			.getLog(BundleClassLoaderDataSourceFactory.class);

	/**
	 * Load the driver from the given bundle.
	 *
	 * @param bundle
	 *            the bundle whose class loader contains the driver
	 * @param config
	 *            the configuration properties
	 * @return a JDBC driver instance
	 * @throws ConfigurationException
	 *             if the driver cannot be loaded
	 */
	@SuppressWarnings("unchecked")
	protected Driver loadDriver(Bundle bundle, Properties config)
			throws ConfigurationException {

		String driverClassName = config.getProperty(FLYWAY_DRIVER_PROPERTY);
		if (driverClassName == null) {
			throw new FlywayException("The '" + FLYWAY_DRIVER_PROPERTY
					+ "' property not defined!");
		}

		try {
			Class<Driver> driverClass = (Class<Driver>) bundle
					.loadClass(driverClassName);
			Driver driver = driverClass.newInstance();
			LOG.debug("Loaded the driver '" + driverClassName
					+ "' from the bundle");
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
	 * Create a Flyway DriverDataSource instance
	 *
	 * @throws ConfigurationException
	 *             if there is a configuration problem found when creating the
	 *             data source
	 */
	@Override
	public DataSource createDataSource(Bundle bundle,
			Properties flywayProperties) {

		String url = flywayProperties.getProperty(Flyway.FLYWAY_URL_PROPERTY);
		String user = flywayProperties.getProperty(Flyway.FLYWAY_USER_PROPERTY);
		String password = flywayProperties
				.getProperty(Flyway.FLYWAY_PASSWORD_PROPERTY);

		// FIXME add support for initSql's
		try {
			Driver driver = loadDriver(bundle, flywayProperties);
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			Properties jdbcProperties = getJdbcProperties(flywayProperties);
			return new DriverDataSource(classLoader, driver, url, user,
					password, jdbcProperties);
		} catch (ConfigurationException e) {
			throw new FlywayException("Could not create flyway datasource", e);
		}
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
	protected Properties getJdbcProperties(Properties config) {
		Properties properties = new Properties();
		Enumeration<Object> keys = config.keys();
		String key;
		while (keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			// filter all Flyway properties
			if (!key.startsWith(Flyway.FLYWAY_PROPERTY_PREFIX)) {
				properties.setProperty(key, config.getProperty(key));
			}
		}

		// remove OSGI specific properties added by ConfigurationAdmin when
		// persisting defaults
		properties.remove(ConfigurationAdmin.SERVICE_FACTORYPID);
		properties.remove(Constants.SERVICE_PID);
		properties.remove(
				FlywayConfiguration.FELIX_FILEINSTALL_FILENAME_PROPERTY);

		return properties;
	}

}

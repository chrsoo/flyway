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
import java.sql.SQLException;
import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.jdbc.DataSourceFactory;

public class FlywayCompendiumConfigurationFactory extends FlywayConfigurationFactory {

	private static final Log LOG = LogFactory.getLog(FlywayCompendiumConfigurationFactory.class);

	/**
	 * <p>
	 * The {@value #FLYWAY_DRIVER_NAME_PROPERTY} property is used in the Flyway
	 * data source configuration files to indicate the name of the JDBC driver
	 * to be used for the data source configuration.
	 */
	public static final String FLYWAY_DRIVER_NAME_PROPERTY = Flyway.FLYWAY_PROPERTY_PREFIX + "driverName";

	/**
	 * <p>
	 * The {@value #FLYWAY_DRIVER_VERSION_PROPERTY} property is used in the
	 * Flyway data source configuration files to indicate the version of the
	 * JDBC driver to be used for the data source configuration.
	 */
	public static final String FLYWAY_DRIVER_VERSION_PROPERTY = Flyway.FLYWAY_PROPERTY_PREFIX + "driverVersion";


	public FlywayCompendiumConfigurationFactory(BundleContext context) {
		super(context);
	}

	// -- FlywayConfigurationFactory

	@Override
	protected Driver loadDriver(Bundle bundle, Properties config) throws ConfigurationException {
		String driverClassName = config.getProperty(FLYWAY_DRIVER_PROPERTY);

		// use the OSGI Compendium JDBC Service
		Driver driver = loadCompendiumDriver(config);
		if(driver != null) {
			LOG.info("Loaded the driver '" + driverClassName
					+  "' using the OSGI Compendium JDBC service");
			return driver;
		}

		// fall back on bundle class loading
		LOG.info("Could not load the driver '" + driverClassName +
					"' using the OSGI Compendium JDBC service, "
					+ "falling back to Bundle classpath loading");

		return super.loadDriver(bundle, config);
	}

	private Driver loadCompendiumDriver(Properties config) throws ConfigurationException {
		Properties properties = getJdbcProperties(config);
		ServiceReference serviceReference = lookupDataSourceFactory(config);
		try {
			DataSourceFactory dataSourceFactory = (DataSourceFactory) context.getService(serviceReference);
			return dataSourceFactory == null ? null : dataSourceFactory.createDriver(properties);
		} catch (SQLException e) {
			LOG.warn(e.getMessage());
			throw new ConfigurationException(FLYWAY_DRIVER_PROPERTY,
					"Caught SQLException when creating the JDBC driver using the "
							+ "OSGI Compendium JDBC Service's DataSourceFactory", e);
		} finally {
			if (serviceReference != null) {
				context.ungetService(serviceReference);
			}
		}
	}

	/**
	 * Lookup the OSGI Compendium JDBC DataSourceFactory based on a given class
	 * name.
	 */
	private ServiceReference lookupDataSourceFactory(Properties config) throws ConfigurationException {

		OsgiJdbcServiceFilter filter = new OsgiJdbcServiceFilter(config, FLYWAY_DRIVER_PROPERTY,
				FLYWAY_DRIVER_NAME_PROPERTY, FLYWAY_DRIVER_VERSION_PROPERTY);

		try {
			ServiceReference[] serviceReferences = context.getServiceReferences(DataSourceFactory.class.getName(),
					filter.toString());

			if(serviceReferences == null || serviceReferences.length == 0) {
				throw new ConfigurationException(FLYWAY_DRIVER_PROPERTY,
						"No OSGI JDBC Compendium DataSourceFactory found for filter '" + filter + "'");
			}

			if (serviceReferences.length > 1) {
				LOG.warn("Found multiple DataSourceFactory services for filter '" + filter
						+ "', selecting the instance first returned");
			}

			return serviceReferences[0];
		} catch (InvalidSyntaxException e) {
			throw new ConfigurationException(FLYWAY_DRIVER_PROPERTY,
					"Could not lookup the OSGI Compendium JDBC DataSourceFactory", e);
		}
	}

}

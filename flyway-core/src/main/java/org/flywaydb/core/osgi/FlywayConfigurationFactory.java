package org.flywaydb.core.osgi;

import static org.flywaydb.core.Flyway.FLYWAY_DRIVER_PROPERTY;

import java.sql.Driver;
import java.sql.SQLException;
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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Registers FLyway data source configurations for the Managed Service Factory
 * PID {@value #FLYWAY_FACTORY_PID}.
 */
public class FlywayConfigurationFactory implements ManagedServiceFactory, FlywayFactory {
	
	private static final Log LOG = LogFactory.getLog(FlywayConfigurationFactory.class);
	
	public static final String FLYWAY_FACTORY_PID = "org.flywaydb.datasource";
	
	private static final String FLYWAY_PROPERTY_PREFIX = "flyway.";
	
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

	private BundleContext context;

	private Map<String, Properties> configurations = Collections.synchronizedMap(new HashMap<String, Properties>());

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
	public void updated(String pid, Dictionary<String, ?> managedServiceConfig) throws ConfigurationException {
		
		LOG.debug("Updating Flyway configuration for pid '" + pid + "'");
		
		String name = getFlywayName(pid);
		LOG.info("Found Flyway configuration for data source '" + name + "'");

		Properties flywayConf = getFlywayConf(managedServiceConfig);

		synchronized (configurations) {
			// old configurations are discarded on purpose, even if the new one
			// fails
			configurations.put(name, flywayConf);
			try {
				// test the configuration
				createFlywayDataSource(flywayConf);
			} catch (FlywayException e) {
				configurations.remove(name);
				LOG.warn("Updating Flyway data source configuration for '" + name + "' failed with error '"
						+ e.getMessage() + "'");
				throw e;
			}
		}

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
	public Flyway create(Bundle bundle, String name, Properties defaultConf) throws FlywayException {

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
		DataSource dataSource = createFlywayDataSource(conf);

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
	 */
	private DataSource createFlywayDataSource(Properties config) {
		
		String url = getConfigValue(config, Flyway.FLYWAY_URL_PROPERTY);
		String user = getConfigValue(config, Flyway.FLYWAY_USER_PROPERTY);
		String password = getConfigValue(config, Flyway.FLYWAY_PASSWORD_PROPERTY);

		// FIXME add support for initSql's
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Driver driver = loadDriver(config);
			return new DriverDataSource(classLoader, driver, url, user, password);
		} catch (ConfigurationException e1) {
			LOG.warn(e1.getMessage());
			LOG.warn("Falling back to default classloading mechanism in DriverDataSource");
			String driverClass = getConfigValue(config, FLYWAY_DRIVER_PROPERTY);
			try {
				return new DriverDataSource(classLoader, driverClass, url, user, password);
			} catch (Exception e2) {
				LOG.error("Could not load JDBC driver using OSGI Compendium's JDBC Service", e1);
				LOG.error("Could not load JDBC driver using the DriverDataSource default classloading mechanism", e2);
				throw new FlywayException("Could not create JDBC Driver", e1);
			}
		}

	}

	private Driver loadDriver(Properties config) throws ConfigurationException {
		
		ServiceReference serviceReference = lookupDataSourceFactory(config);
		Properties properties = getJdbcProperties(config);
		try {
			DataSourceFactory dataSourceFactory = (DataSourceFactory) context.getService(serviceReference);
			return dataSourceFactory.createDriver(properties);
		} catch (SQLException e) {
			LOG.warn(e.getMessage());
			throw new ConfigurationException(FLYWAY_DRIVER_PROPERTY,
					"Caught SQLException when creating the JDBC driver using the "
							+ "OSGI Compendium JDBC Service's DataSourceFactory",
					e);
		} finally {
			if (serviceReference != null) {
				context.ungetService(serviceReference);
			}
		}
	}

	/**
	 * Lookup the OSGI Compendium JDBC DataSourceFactory based on a given class
	 * name
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

	/**
	 * Filter Flyway and OSGI specific configuration from the JDBC properties.
	 * Specifically the <code>service.factoryPid</code> property and all
	 * properties prefixed with {@value #FLYWAY_PROPERTY_PREFIX} and are
	 * removed.
	 * 
	 * @return a new Properties instance containing only JDBC configuration
	 *         properties.
	 */
	private Properties getJdbcProperties(Properties config) {
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

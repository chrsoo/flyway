/**
 * Copyright 2012 Richemont
 */
package org.flywaydb.core.osgi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.bundle.BundleScanner;
import org.osgi.framework.Bundle;

public class FlywayBundleServiceBean implements FlywayBundleService {

	private static final Log LOG = LogFactory
			.getLog(FlywayBundleServiceBean.class);
	private FlywayConfigurationService configService;
	private DataSourceFactoryStrategy dataSourceFactory;

	public FlywayBundleServiceBean(DataSourceFactoryStrategy dataSourceFactory,
			FlywayConfigurationService configService) {
		this.dataSourceFactory = dataSourceFactory;
		this.configService = configService;
	}

	@Override
	public List<FlywayBundleConfiguration> scan(Bundle bundle) {

		List<FlywayBundleConfiguration> configurations = new ArrayList<FlywayBundleConfiguration>();

		configurations.addAll(scan(bundle, "META-INF/flyway"));
		configurations.addAll(scan(bundle, "WEB-INF/classes/META-INF/flyway"));

		if (configurations.isEmpty()) {
			LOG.debug("No Flyway migrations found for bundle "
					+ bundle.getBundleId());

			return configurations;
		} else {
			LOG.debug("Found " + configurations.size()
					+ " Flyway migrations found for bundle "
					+ bundle.getBundleId());

			return configurations;
		}

	}

	@SuppressWarnings("unchecked")
	private List<FlywayBundleConfiguration> scan(Bundle bundle, String path) {

		List<FlywayBundleConfiguration> configurations = new ArrayList<FlywayBundleConfiguration>();

		Enumeration<URL> entries = bundle.findEntries(path, "*.properties",
				false);

		if (entries == null || !entries.hasMoreElements()) {
			LOG.debug("No Flyway migrations found for bundle "
					+ bundle.getBundleId() + " and path '" + path + "'");
			return configurations;
		}

		FlywayBundleConfiguration config;
		while (entries.hasMoreElements()) {
			config = new FlywayBundleConfiguration(entries.nextElement());
			LOG.debug("Found Flyway configuration '" + config.getName()
					+ "' for URL '" + config.getUrl() + "'");
			configurations.add(config);
		}

		LOG.debug("Found " + configurations.size()
				+ " Flyway migrations found for bundle " + bundle.getBundleId()
				+ " and path '" + path + "'");

		return configurations;
	}

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
		FlywayConfiguration managedConfig = configService
				.getConfiguration(name);
		if (managedConfig == null) {
			LOG.warn("Could not find managed configuration for Flyway '" + name
					+ "'");
		} else {
			flywayProperties.putAll(managedConfig.getProperties());
		}

		// create the DataSource before we remove the JDBC driver and URL
		DataSource dataSource = dataSourceFactory.createDataSource(bundle,
				flywayProperties);
		flyway.setDataSource(dataSource);

		// apply the properties to the Flyway instance
		flyway.configure(flywayProperties);

		// set the Scanner strategy
		BundleScanner scanner = new BundleScanner(bundle);
		flyway.setScanner(scanner);

		return flyway;
	}

	// -- FlywayDatasourceServiceFactory


}

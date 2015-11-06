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

import java.util.Dictionary;
import java.util.Properties;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * Starts the following Flyway services
 * <ul>
 * <li>FlywayManagedServiceFactory with the PID
 * {@value FlywayConfigurationManagedServiceFactory#FLYWAY_FACTORY_PID}
 * <li>BundleTracker for tracking Flyway Migrations
 * </ul>
 *
 */
public class FlywayBundleActivator implements BundleActivator {

	private static final String DATA_SOURCE_FACTORY_CLASS_NAME = "org.osgi.service.jdbc.DataSourceFactory";
	private static final Log LOG = LogFactory.getLog(FlywayBundleActivator.class);
	private BundleTracker tracker;

	// -- BundleActivator

	@Override
	public void start(BundleContext context) throws Exception {

		// Register the ManagedServiceFactory
		FlywayConfigurationService configService = registerManagedService(
				context);

		// Register the FlywayBundleService
		FlywayBundleService bundleService = registerBundleService(context,
				configService);


		// Register the FlywayOsgiExtender
		registerOsgiExtender(context, bundleService);

	}

	private void registerOsgiExtender(BundleContext context,
			FlywayBundleService bundleService) {

		BundleTrackerCustomizer customizer = new FlywayOsgiExtender(
				bundleService);

		tracker = new BundleTracker(context, Bundle.STARTING, customizer);
		tracker.open();

		LOG.info("Registered FlywayOsgiExtender BundleTracker");
	}

	private FlywayConfigurationManagedServiceFactory registerManagedService(
			BundleContext context) {
		FlywayConfigurationManagedServiceFactory configurationFactory = new FlywayConfigurationManagedServiceFactory(
				context);

		Properties factoryProperties = new Properties();
		factoryProperties.put(Constants.SERVICE_PID,
				FlywayConfigurationManagedServiceFactory.FLYWAY_FACTORY_PID);

		context.registerService(ManagedServiceFactory.class.getName(),
				configurationFactory, (Dictionary) factoryProperties);
		LOG.info("Registered the FlywayConfiguration ManagedServiceFactory");
		return configurationFactory;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private FlywayBundleService registerBundleService(BundleContext context,
			FlywayConfigurationService configService) {

		// Avoid the ClassNotFoundException if Compendium Services are not
		// available
		DataSourceFactoryStrategy dataSourceFactory = isJdbcCompendiumService(context)
				? new CompendiumServiceDataSourceFactory(context)
				: new BundleClassLoaderDataSourceFactory();

		FlywayBundleService bundleService = new FlywayBundleServiceBean(
				dataSourceFactory, configService);

		context.registerService(FlywayBundleService.class.getName(),
				bundleService, (Dictionary) new Properties());
		LOG.info("Registered the FlywayBundleService ManagedServiceFactory");
		return bundleService;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		tracker.close();
	}

	// -- FlywayBundleActivator

	/**
	 * Find out if the optional DataSourceFactory class is available; avoids ClassNotFoundExceptions
	 * @return true if org.osgi.service.jdbc.DataSourceFactory can be loaded
	 */
	private boolean isJdbcCompendiumService(BundleContext context) {
		try {
			Bundle bundle = context.getBundle();
			bundle.loadClass(DATA_SOURCE_FACTORY_CLASS_NAME);
			LOG.debug("The class '"+ DATA_SOURCE_FACTORY_CLASS_NAME +"' can be loaded");
			return true;
		} catch(ClassNotFoundException e) {
			LOG.debug("The class '"+ DATA_SOURCE_FACTORY_CLASS_NAME +"' cannot be loaded");
			return false;
		}
	}


}

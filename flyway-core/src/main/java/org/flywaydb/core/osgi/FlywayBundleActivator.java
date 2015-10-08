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
 * <li>FlywayManagedServiceFactory with the PID {@value FlywayConfigurationFactory#FLYWAY_FACTORY_PID}
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

		Properties factoryProperties = new Properties();
		factoryProperties.put(Constants.SERVICE_PID,
				FlywayConfigurationFactory.FLYWAY_FACTORY_PID);

		FlywayConfigurationFactory configurationFactory = isJdbcCompendiumService(context)
				? new FlywayCompendiumConfigurationFactory(context)
				: new FlywayConfigurationFactory(context);

		context.registerService(ManagedServiceFactory.class.getName(), configurationFactory, factoryProperties);
		LOG.info("Registered FlywayConfigurationFactory ManagedServiceFactory instance");

		BundleTrackerCustomizer customizer = new FlywayOsgiExtender(configurationFactory);
		tracker = new BundleTracker(context, Bundle.STARTING, customizer);
		tracker.open();
		LOG.info("Registered FlywayOsgiExtender BundleTracker instance");

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

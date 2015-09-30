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

	private static final Log LOG = LogFactory.getLog(FlywayBundleActivator.class);
	private BundleTracker tracker;
	
	// -- BundleActivator

	@Override
	public void start(BundleContext context) throws Exception {

		Properties factoryProperties = new Properties();
		factoryProperties.put(Constants.SERVICE_PID, FlywayConfigurationFactory.FLYWAY_FACTORY_PID);

		FlywayConfigurationFactory configurationFactory = new FlywayConfigurationFactory(context);
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

}

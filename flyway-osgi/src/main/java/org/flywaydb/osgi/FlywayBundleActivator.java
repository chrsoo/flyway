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
package org.flywaydb.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.osgi.configuration.FlywayConfigurationService;
import org.flywaydb.osgi.configuration.FlywayConfigurationServiceBean;
import org.flywaydb.osgi.extender.FlywayBundleConfigurationExtender;
import org.flywaydb.osgi.extender.FlywayBundleConfigurationScanner;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * Starts the following Flyway services
 * <ul>
 * <li>FlywayManagedServiceFactory with the PID
 * {@value FlywayConfigurationServiceBean#FLYWAY_FACTORY_PID}
 * <li>BundleTracker for tracking Flyway Migrations
 * </ul>
 *
 */
public class FlywayBundleActivator implements BundleActivator {

	private static final Log LOG = LogFactory.getLog(FlywayBundleActivator.class);

	private final List<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();

	private BundleTracker tracker;

	// -- BundleActivator

	@Override
	public void start(BundleContext context) throws Exception {

		FlywayServiceRegistry registry = registerFlywayServiceRegistry(context);

		// Register the ManagedServiceFactory
		FlywayConfigurationService configService = registerConfigurationService(
				context);

		// Register the FlywayServiceFactory
		OsgiFlywayServiceFactory flywayServiceFactory = registerFlywayServiceFactory(
				context, configService);

		// Register the FlywayOsgiExtender
		registerOsgiExtender(context, flywayServiceFactory, registry);

		// // Register the Blueprint Namespace Handler
		// FlywayNamespaceHandler handler = new FlywayNamespaceHandler(
		// flywayServiceFactory);
		// registerBlueprintNamespaceHandler(context, handler,
		// FlywayNamespaceHandler.FLYWAY_NS);
	}

	@Override
	public void stop(BundleContext context) throws Exception {

		for (ServiceRegistration registration : registrations) {
			try {
				registration.unregister();
			} catch (Exception e) {
				LOG.warn("Exception caught while unregistering service: " +
						e.getMessage());
			}
		}

		tracker.close();
	}

	private FlywayServiceRegistry registerFlywayServiceRegistry(
			BundleContext context) {
		FlywayServiceRegistryBean registry = new FlywayServiceRegistryBean();

		Properties properties = new Properties();
		ServiceRegistration registration = context.registerService(
				FlywayServiceRegistry.class.getName(), registry, properties);

		registrations.add(registration);

		return registry;
	}

	// -- FlywayBundleActivator

	private OsgiFlywayServiceFactory registerFlywayServiceFactory(
			BundleContext context,
			FlywayConfigurationService configService) {

		OsgiDataSourceFactory dataSourceFactory = new OsgiDataSourceFactory();
		OsgiFlywayServiceFactoryBean flywayServiceFactory = new OsgiFlywayServiceFactoryBean(
				configService, dataSourceFactory);

		Properties properties = new Properties();

		ServiceRegistration registration = context.registerService(
				OsgiFlywayServiceFactory.class.getName(),
				flywayServiceFactory, properties);

		registrations.add(registration);

		return flywayServiceFactory;
	}

	private void registerOsgiExtender(BundleContext context,
			OsgiFlywayServiceFactory factory, FlywayServiceRegistry registry) {

		FlywayBundleConfigurationScanner bundleScanner = new FlywayBundleConfigurationScanner();
		BundleTrackerCustomizer customizer = new FlywayBundleConfigurationExtender(
				bundleScanner, factory, registry);

		int mask = BundleState.getMask(
				BundleState.RESOLVED,
				BundleState.STARTING,
				BundleState.ACTIVE,
				BundleState.STOPPING);

		tracker = new BundleTracker(context, mask, customizer);
		tracker.open();

		LOG.info("Registered FlywayOsgiExtender BundleTracker");
	}

	private FlywayConfigurationServiceBean registerConfigurationService(
			BundleContext context) {

		FlywayConfigurationServiceBean configurationFactory = new FlywayConfigurationServiceBean(
				context);

		Properties factoryProperties = new Properties();
		factoryProperties.put(Constants.SERVICE_PID,
				FlywayConfigurationServiceBean.FLYWAY_FACTORY_PID);

		ServiceRegistration registration = context
				.registerService(new String[] {
				ManagedServiceFactory.class.getName(),
						FlywayConfigurationService.class.getName() },
				configurationFactory, factoryProperties);

		registrations.add(registration);

		LOG.info("Registered the FlywayConfigurationServce "
				+ "and ManagedServiceFactory");

		return configurationFactory;
	}

}

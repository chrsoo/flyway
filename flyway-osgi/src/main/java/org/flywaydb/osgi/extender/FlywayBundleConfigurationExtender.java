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
package org.flywaydb.osgi.extender;

import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.osgi.BundleEventType;
import org.flywaydb.osgi.BundleState;
import org.flywaydb.osgi.FlywayService;
import org.flywaydb.osgi.FlywayServiceRegistry;
import org.flywaydb.osgi.OsgiFlywayServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;

public class FlywayBundleConfigurationExtender implements BundleTrackerCustomizer {

	private static final Log LOG = LogFactory.getLog(FlywayBundleConfigurationExtender.class);

	private final FlywayBundleConfigurationScanner configScanner;
	private final OsgiFlywayServiceFactory serviceFactory;
	private final FlywayServiceRegistry registry;

	public FlywayBundleConfigurationExtender(
			FlywayBundleConfigurationScanner scanner,
			OsgiFlywayServiceFactory factory, FlywayServiceRegistry registry) {

		this.configScanner = scanner;
		this.serviceFactory = factory;
		this.registry = registry;
	}

	// -- BundleTrackerCustomizer

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {

		logEventForMethod("addingBundle", bundle, event);

		List<FlywayBundleConfiguration> configurations = configScanner
				.scan(bundle);
		List<FlywayService> services = new ArrayList<FlywayService>(
				configurations.size());
		FlywayService service;
		for (FlywayBundleConfiguration config : configurations) {
			service = serviceFactory.create(bundle, config.getName(),
					config.loadProperties());
			services.add(service);
		}

		registry.register(services);

		return services;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
		logEventForMethod("modifiedBundle", bundle, event);

		if (bundle.getState() == Bundle.STARTING) {
			List<FlywayService> services = (List<FlywayService>) object;
			for (FlywayService service : services) {
				service.init();
			}
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {

		logEventForMethod("removedBundle", bundle, event);

		List<FlywayService> services = (List<FlywayService>) object;
		registry.unregister(services);
		services.clear();

	}

	// -- FlywayOsgiExtender

	private void logEventForMethod(String method, Bundle bundle,
			BundleEvent event) {

		BundleState state = BundleState.get(bundle.getState());
		BundleEventType eventType = BundleEventType.get(event);

		LOG.debug("Callback method '" + method + "' invoked for '" + eventType
				+ "' event and bundle " + bundle.getBundleId()
				+ " in state '" + state + "' ");
	}

}

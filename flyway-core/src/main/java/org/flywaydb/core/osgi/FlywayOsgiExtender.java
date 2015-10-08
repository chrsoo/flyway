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

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;

public class FlywayOsgiExtender implements BundleTrackerCustomizer {

	private static final Log LOG = LogFactory.getLog(FlywayOsgiExtender.class);

	private final FlywayFactory factory;

	public FlywayOsgiExtender(FlywayFactory factory) {
		this.factory = factory;
	}

	// -- BundleTrackerCustomizer

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		return migrate(bundle, event);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
		// cast should always work or there is a bug in the tracker
		List<String> configurationList = (List<String>) object;

		String configurations = configurationListToString(configurationList);
		LOG.debug("Flyway bundle modified for configurations " + configurations);

		List<String> newConfigurationList = migrate(bundle, event);

		boolean changed;
		if(newConfigurationList == null) {
			changed = configurationList.size() < 0;
			configurationList.clear();
		} else {
			changed = configurationList.retainAll(newConfigurationList);
		}

		String message = "Flyway bundle " + bundle.getBundleId() + " updated; ";
		if(changed) {
			String newConfigurations = configurationListToString(newConfigurationList);
			message += "changed from [" + configurations + "] to [" + newConfigurations + "]";
		} else {
			message += "previous list of configurations retained [" + configurations + "]";
		}

		LOG.info(message);

	}

	@Override
	@SuppressWarnings("unchecked")
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		String configurations = StringUtils.collectionToCommaDelimitedString((List<String>) object);
		LOG.info("Removed Flyway '" + configurations
				+ "' from " + bundle.getBundleId());
	}

	// -- FlywayOsgiExtender

	// FIXME Somehow stop the bundle when an exception is thrown!
	private List<String> migrate(Bundle bundle, BundleEvent event) {

		BundleState state = BundleState.get(bundle.getState());
		BundleEventType eventType = BundleEventType.get(event);

		LOG.debug("Received '" + eventType
				+ "' event for bundle " + bundle.getBundleId()
				+ " in state '" + state + "'");

		try {
			// we assume that we only receive events when we are in the STARTING state
			return migrate(bundle);
		} catch(Exception e) {
			LOG.error("Caught unhandled exception while executing Flyway migrate", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> migrate(Bundle bundle) {

		List<String> configurations = new ArrayList<String>();
		Enumeration<URL> entries = bundle.findEntries("META-INF/flyway", "*.properties", false);

		if(entries == null || !entries.hasMoreElements()) {
			LOG.debug("No Flyway migrations found for bundle " + bundle.getBundleId());
			return null;
		}

		FlywayBundleConfiguration config;
		while (entries.hasMoreElements()) {
			config = new FlywayBundleConfiguration(entries.nextElement());
			LOG.debug("Found Flyway configuration '" + config.getName()
				+ "' for URL '" + config.getUrl() + "'");
			String configuration = migrate(bundle, config);
			configurations.add(configuration);
		}

		LOG.debug("Found " + configurations.size()
				+ " Flyway migrations found for bundle "
				+ bundle.getBundleId());

		return configurations;
	}

	/**
	 * Migrate the configuration URL
	 *
	 * @param bundle the bundle that owns the configuration
	 * @param configUrl the URL to the Flyway configuration properties file
	 * @return the name of the configuration that was migrated
	 */
	private String migrate(Bundle bundle, FlywayBundleConfiguration config) {

		Flyway flyway = factory.create(bundle, config);

		LOG.debug("Migrating '" + config.getName() + "'");
		flyway.migrate();

		LOG.info("Migrated '" + config.getName() + "'");
		return config.getName();
	}

	private String configurationListToString(List<String> configurationList) {
		return configurationList == null || configurationList.isEmpty()
				? "<empty>"
				: StringUtils.collectionToCommaDelimitedString(configurationList);
	}
}

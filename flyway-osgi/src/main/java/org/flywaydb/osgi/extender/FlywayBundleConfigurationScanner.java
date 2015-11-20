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

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.osgi.framework.Bundle;

public class FlywayBundleConfigurationScanner {

	private static final Log LOG = LogFactory
			.getLog(FlywayBundleConfigurationScanner.class);

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

}

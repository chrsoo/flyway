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

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Registers Flyway configurations for the Managed Service Factory PID
 * {@value #FLYWAY_FACTORY_PID}.
 */
public class FlywayConfigurationManagedServiceFactory
		implements ManagedServiceFactory, FlywayConfigurationService {

	private static final Log LOG = LogFactory.getLog(FlywayConfigurationManagedServiceFactory.class);

	public static final String FLYWAY_FACTORY_PID = "org.flywaydb.core";

	protected final BundleContext context;

	private final Map<String, FlywayConfiguration> configurations =
			Collections.synchronizedMap(new HashMap<String, FlywayConfiguration>());

	// -- Constructors

	public FlywayConfigurationManagedServiceFactory(BundleContext context) {
		this.context = context;
	}

	// -- ManagedServiceFactory

	@Override
	public String getName() {
		return "Flyway Configuration Managed Service Factory";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void updated(String pid, Dictionary config) throws ConfigurationException {

		FlywayConfiguration flywayConf = new FlywayConfiguration(config);

		String name = flywayConf.getName();
		if(name == null) {
			LOG.warn("Flyway configuration for pid " + pid
					+ " must define the '"
					+ FlywayConfiguration.FLYWAY_NAME_PROPERTY
					+ "' property");
			return;
		}

		configurations.put(name, flywayConf);
		LOG.info("Updated Flyway configuration '" + name + "' with pid '" + pid + "'");
	}

	@Override
	public void deleted(String pid) {
		String name = getFlywayName(pid);
		configurations.remove(name);
		LOG.info("Removed the Flyway configuration for '" + name + "'");
	}


	/**
	 * Check all Flyway configurations for a pid that matches
	 * and return the Flyway configuration name.
	 */
	private String getFlywayName(String pid) {
		String configPid;

		for(FlywayConfiguration flywayConf: configurations.values()) {
			configPid = flywayConf.get(Constants.SERVICE_PID);
			if(configPid.equals(pid)) {
				return flywayConf.getName();
			}
		}

		throw new FlywayException("Could not find configuration for pid '" + pid + "'");
	}

	// -- FlywayConfiguration

	@Override
	public FlywayConfiguration getConfiguration(String name) {
		return configurations.get(name);
	}

}

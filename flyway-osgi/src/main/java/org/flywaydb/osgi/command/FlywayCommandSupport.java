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
package org.flywaydb.osgi.command;

import java.io.IOException;
import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.osgi.FlywayService;
import org.flywaydb.osgi.FlywayServiceRegistry;

import jline.console.ConsoleReader;

abstract class FlywayCommandSupport extends OsgiCommandSupport {

	private static final Log LOG = LogFactory
			.getLog(FlywayServiceCompleter.class);

	private String command;

	@Argument(index = 0, required = true, name = "name", description = "The Flyway Service name", multiValued = false)
	protected String name;

	@Argument(index = 1, required = false, name = "bundleId", description = "The Bundle ID", multiValued = false)
	protected long bundleId = -1;

	@Option(name = "--force", aliases = {
			"-f" }, description = "Forces the command to execute", required = false, multiValued = false)
	protected boolean force;

	public FlywayCommandSupport(String command, boolean force) {
		super();
		this.command = command;
		this.force = force;
	}

	protected abstract String doExecute(FlywayService flyway) throws Exception;

	@Override
	protected final String doExecute() throws Exception {

		try {
			String result;
			if (isConfirmed()) {

				FlywayService service = getFlywayService();

				result = doExecute(service);

				// prefix result with semicolon if not null or empty
				result = StringUtils.hasText(result)
						? "; " + result
						: "";

				result = "Executed Flyway " + command + " for '" + name
						+ "'" + result;

			} else {
				result = "Skipped Flyway " + command + " for '" + name
						+ "'";
			}

			return result;

		} finally {
			ungetServices();
		}
	}

	private FlywayService getFlywayService() throws Exception {

		List<FlywayServiceRegistry> serivceRegistries = getAllServices(
				FlywayServiceRegistry.class,
				null);

		if (serivceRegistries == null) {
			throw new RuntimeException("Flyway Service Registry not found!");
		}

		return getFlywayService(serivceRegistries);
	}

	private FlywayService getFlywayService(
			List<FlywayServiceRegistry> serivceRegistries) {

		if (serivceRegistries.size() == 1) {
			FlywayServiceRegistry registry = serivceRegistries.get(0);
			List<FlywayService> services = registry.findByName(name);
			switch (services.size()) {
			case 0: // no match
				throw new RuntimeException("Could not find Flyway Service "
						+ "with name '" + name + "'");
			case 1: // exact match
				return services.get(0);
			default: // more than one Flyway Service returned
				if (bundleId == -1) {
					throw new IllegalArgumentException(
							"More than one Flyway Service returned for name '"
									+ name + "', please specify a bundle id!");
				}
				// return the service that matched the bundleId
				for (FlywayService service : services) {
					if (service.getBundleId() == bundleId) {
						return service;
					}
				}
				// no matching service found
				throw new RuntimeException("Could not find Flyway Service "
						+ "with name '" + name + "' and bundle id " + bundleId);
			}
		} else {
			// too many FlywayServiceRegistries, multiple flyway-osgi bundles
			// active?
			throw new RuntimeException(
					"Expcected exactly one FlywayServiceRegistry but found "
							+ serivceRegistries.size());
		}
	}

	protected boolean isConfirmed() throws IOException {

		// always confirmed if forcing
		if (force) {
			return true;
		}

		while(true) {
			ConsoleReader reader = (ConsoleReader) session.get(".jline.reader");

			String msg = "Please confirm (yes/no): ";
			String str = reader.readLine(msg);
			if ("yes".equalsIgnoreCase(str)) {
				return true;
			}
			if ("no".equalsIgnoreCase(str)) {
				return false;
			}
		}

	}

}

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

import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.flywaydb.osgi.FlywayService;
import org.flywaydb.osgi.FlywayServiceRegistry;

import jline.console.ConsoleReader;

public class FlywayListCommand extends OsgiCommandSupport {

	@Argument(index = 0, required = false, name = "bundleId", description = "The Bundle ID", multiValued = false)
	protected long bundleId = -1;

	public FlywayListCommand() {
	}

	@Override
	protected final String doExecute() throws Exception {
		try {

			FlywayServiceRegistry registry = getFlywayServiceRegistry();

			List<FlywayService> services = bundleId == -1
					? registry.list()
					: registry.findByBundle(bundleId);

			if (services.size() == 0) {
				return "No Flyway Services found!";
			}

			for (FlywayService service : services) {
				ConsoleReader reader = (ConsoleReader) session
						.get(".jline.reader");
				reader.println(service.toString());
			}

			return null;

		} finally {
			ungetServices();
		}
	}

	private FlywayServiceRegistry getFlywayServiceRegistry() throws Exception {

		List<FlywayServiceRegistry> serivceRegistries = getAllServices(
				FlywayServiceRegistry.class,
				null);

		if (serivceRegistries == null) {
			throw new RuntimeException("Flyway Service Registry not found!");
		}

		if (serivceRegistries.size() == 1) {
			return serivceRegistries.get(0);
		}

		throw new RuntimeException(
				"Expcected exactly one FlywayServiceRegistry but found "
						+ serivceRegistries.size());
	}

}

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
package org.flywaydb.core.osgi.karaf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.osgi.FlywayBundleConfiguration;
import org.flywaydb.core.osgi.FlywayBundleService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

abstract class FlywayKarafCommandSupport extends OsgiCommandSupport {

	@Argument(index = 0, required = true, name = "bundle", description = "The Bundle ID", multiValued = false)
	protected int bundleId;

	@Argument(index = 1, required = false, name = "migration", description = "The Flyway migration", multiValued = false)
	protected String migration;

	private boolean requiresConfirmation = false;
	private String command;

	public FlywayKarafCommandSupport(String command,
			boolean requiresConfirmation) {
		super();
		this.command = command;
		this.requiresConfirmation = requiresConfirmation;
	}

	@Override
	protected final String doExecute() throws Exception {

		try {
			ServiceReference reference = bundleContext
					.getServiceReference(FlywayBundleService.class.getName());
			FlywayBundleService service = super.getService(
					FlywayBundleService.class, reference);

			Bundle bundle = getBundle();
			FlywayBundleConfiguration config = getConfig(service, bundle);

			Flyway flyway = service.create(bundle, config);
			String result;
			if (isConfirmed(flyway)) {
				result = doExecute(flyway);

				// prefix result with semicolon if not null or empty
				result = StringUtils.hasText(result)
						? "; " + result
						: "";

				result = "Executed Flyway " + command + " for the '" + migration
						+ "' migration" + result;

			} else {
				result = "Skipped Flyway " + command + " for the '" + migration
						+ "' migration";
			}

			return result;

		} finally {
			ungetServices();
		}
	}

	protected abstract String doExecute(Flyway flyway);

	private Bundle getBundle() {
		Bundle bundle = bundleContext.getBundle(bundleId);
		if (bundle == null) {
			throw new IllegalArgumentException(
					"Could not find bundle with id " + bundleId);
		}
		return bundle;
	}

	private FlywayBundleConfiguration getConfig(FlywayBundleService service,
			Bundle bundle) {

		List<FlywayBundleConfiguration> configs = service.scan(bundle);
		if (configs.isEmpty()) {
			throw new RuntimeException(
					"No migrations could be found in bundle " + bundleId + "!");
		}

		if (StringUtils.hasText(migration)) {
			// user has specified the migration, try to match it
			migration = migration.trim();

			for (FlywayBundleConfiguration config : configs) {
				if (config.getName().equals(migration)) {
					return config;
				}
			}
			// no match
			throw new RuntimeException("Multiple migrations found but "
					+ "none matched '" + migration + "'; "
					+ "try the 'info' command or use tab command completion!");

		} else if (configs.size() == 1) {
			// no migration specified but there is only one anyways

			FlywayBundleConfiguration config = configs.get(0);
			migration = config.getName();

			return config;
		} else {
			throw new IllegalArgumentException("Multiple migrations found but "
					+ "none was given as an argument; "
					+ "try the 'info' command or use tab command completion!");
		}

	}

	protected boolean isConfirmed(Flyway flyway) {

		// always confirmed if no confirmation is required!
		if (!requiresConfirmation) {
			return true;
		}

		PrintStream console = session.getConsole();

		console.println("Enter 'yes' if you are sure!");
		console.flush();

		InputStream keyboard = session.getKeyboard();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(keyboard));

		String line = null;
		try {
			line = reader.readLine();
			line = StringUtils.hasText(line)
					? line.trim()
					: "no";

		} catch (IOException e) {
			throw new RuntimeException("Could not read user input!", e);
		}

		return line.equalsIgnoreCase("yes");
	}

}

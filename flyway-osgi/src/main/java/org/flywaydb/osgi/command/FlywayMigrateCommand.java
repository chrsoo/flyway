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

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.osgi.FlywayService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

@Command(name = FlywayMigrateCommand.COMMAND_NAME, scope = "runtime", description = "Starts the database name.", detailedDescription = "Starts the database name. All pending migrations will be applied in order. Calling migrate on an up-to-date database has no effect.")
public class FlywayMigrateCommand extends FlywayCommandSupport {

	@Option(name = "--start", aliases = "-s", description = "Start the bundle after the migration", multiValued = false, required = false)
	private boolean start = false;

	protected static final String COMMAND_NAME = "migrate";

	public FlywayMigrateCommand() {
		super(COMMAND_NAME, false);
	}

	@Override
	protected String doExecute(FlywayService flyway) throws BundleException {

		flyway.migrate();

		MigrationInfo info = flyway.info().current();

		StringBuilder msg = new StringBuilder("schema version is ")
				.append(info.getVersion());

		String description = StringUtils.trimToNull(info.getDescription());
		if (description != null) {
			msg.append(" (").append(description).append(")");
		}

		if (start) {
			BundleContext context = getBundleContext();
			Bundle bundle = context.getBundle(flyway.getBundleId());

			bundle.start();

			msg.append(" - bundle ")
					.append(bundle.getBundleId())
					.append(") started.");

		}

		return msg.toString();
	}

}

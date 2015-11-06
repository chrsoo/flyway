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

import org.apache.felix.gogo.commands.Command;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.util.StringUtils;

@Command(name = FlywayMigrateCommand.COMMAND_NAME, scope = "runtime", description = "Starts the database migration.", detailedDescription = "Starts the database migration. All pending migrations will be applied in order. Calling migrate on an up-to-date database has no effect.")
public class FlywayMigrateCommand extends FlywayKarafCommandSupport {

	protected static final String COMMAND_NAME = "migrate";

	public FlywayMigrateCommand() {
		super(COMMAND_NAME, true);

	}

	@Override
	protected String doExecute(Flyway flyway) {
		flyway.migrate();
		MigrationInfo info = flyway.info().current();

		String description = info.getDescription();
		description = StringUtils.hasText(description)
				? description.trim()
				: null;

		return "schema version is " + info.getVersion()
				+ (description == null ? "" : " ("
						+ info.getDescription() + ")");
	}

}

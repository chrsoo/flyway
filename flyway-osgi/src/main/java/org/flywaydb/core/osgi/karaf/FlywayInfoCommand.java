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

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.internal.info.MigrationInfoDumper;

@Command(name = FlywayInfoCommand.COMMAND_NAME, scope = "runtime", description = "Retrieves the complete information about migrations.", detailedDescription = "Retrieves the complete information about all the migrations including applied, pending and current migrations with details and status.")
public class FlywayInfoCommand extends FlywayKarafCommandSupport {

	protected static final String COMMAND_NAME = "info";

	private enum InfoScope {
		current, applied, all
	}

	public FlywayInfoCommand() {
		super(COMMAND_NAME, false);
	}

	@Argument(index = 2, description = "The scope of the information to return: current, applied or all.", required = false)
	private InfoScope scope;

	@Override
	protected String doExecute(Flyway flyway) {
		MigrationInfoService infoService = flyway.info();

		if (scope == null) {
			scope = InfoScope.current;
		}

		MigrationInfo[] info;
		switch(scope) {
		case current:
			info = new MigrationInfo[] { infoService.current() };
			break;
		case applied:
			info = infoService.applied();
			break;
		case all:
			info = infoService.all();
			break;
		default:
			throw new IllegalStateException(
					"The scope '" + scope + "' is not handled!");
		}

		return info == null
				? "\n" + MigrationInfoDumper.dumpToAsciiTable(info)
				: "\nMigration info not found!";
	}

}

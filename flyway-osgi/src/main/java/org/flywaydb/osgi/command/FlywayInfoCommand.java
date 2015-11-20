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
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.osgi.FlywayService;

@Command(name = FlywayInfoCommand.COMMAND_NAME, scope = "runtime", description = "Retrieves the complete information about migrations.", detailedDescription = "Retrieves the complete information about all the migrations including applied, pending and current migrations with details and status.")
public class FlywayInfoCommand extends FlywayCommandSupport {

	protected static final String COMMAND_NAME = "info";
	public enum InfoScope {
		current, applied, all
	}

	@Option(name = "--scope", aliases = "-s", required = false, multiValued = false, description = "Scope of the information to return ('current', 'applied' or 'all')")
	protected InfoScope scope = InfoScope.current;

	public FlywayInfoCommand() {
		super(COMMAND_NAME, true);
	}

	@Override
	protected String doExecute(FlywayService flyway) {
		MigrationInfoService infoService = flyway.info();

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
				? "migration info not found!"
				: "\n" + MigrationInfoDumper.dumpToAsciiTable(info);

	}

}

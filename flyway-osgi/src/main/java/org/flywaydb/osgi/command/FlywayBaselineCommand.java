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
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.osgi.FlywayService;

@Command(name = FlywayBaselineCommand.COMMAND_NAME, scope = "runtime", description = "Baselines an existing database.", detailedDescription = "Baselines an existing database, excluding all migrations up to and including baselineVersion.")
public class FlywayBaselineCommand extends FlywayCommandSupport {

	protected static final String COMMAND_NAME = "baseline";

	@Option(name = "--version", aliases = "-v", required = false, multiValued = false, valueToShowInHelp = "<version>", description = "The baseline version")
	private String baselineVersion;

	@Option(name = "--description", aliases = "-d", required = false, multiValued = false, valueToShowInHelp = "<description>", description = "The baseline version")
	private String baselineDescription;

	public FlywayBaselineCommand() {
		super(COMMAND_NAME, false);
	}

	@Override
	protected String doExecute(FlywayService flyway) {

		if (StringUtils.hasText(baselineVersion)) {
			MigrationVersion version = MigrationVersion
					.fromVersion(baselineVersion);
			flyway.setBaselineVersion(version);
		}

		if (StringUtils.hasText(baselineDescription)) {
			flyway.setBaselineDescription(baselineDescription);
		}

		MigrationInfoService infoService = flyway.info();
		MigrationInfo[] info = infoService.all();

		String[] schemas = flyway.getSchemas();
		String schema = schemas == null || schemas.length == 0
				? "undefined"
				: schemas[0];

		return "used the schema '" + schema + "'\n"
				+ MigrationInfoDumper.dumpToAsciiTable(info);
	}

}

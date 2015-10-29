/**
 * Copyright 2012 Richemont
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

		return "\n" + MigrationInfoDumper.dumpToAsciiTable(info);
	}

}

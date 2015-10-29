/**
 * Copyright 2012 Richemont
 */
package org.flywaydb.core.osgi.karaf;

import org.apache.felix.gogo.commands.Command;
import org.flywaydb.core.Flyway;

@Command(name = FlywayCleanCommand.COMMAND_NAME, scope = "runtime", description = "Drop all database objects for the migration schema.", detailedDescription = "Drops all objects (tables, views, procedures, triggers, ...) "
		+ "in the configured schemas. The schemas are cleaned in the order "
		+ "specified by the schemas property.")
public class FlywayCleanCommand extends FlywayKarafCommandSupport {

	protected static final String COMMAND_NAME = "baseline";

	public FlywayCleanCommand(String command, boolean requiresConfirmation) {
		super(COMMAND_NAME, true);
	}

	@Override
	protected String doExecute(Flyway flyway) {
		flyway.clean();
		return null;
	}

}

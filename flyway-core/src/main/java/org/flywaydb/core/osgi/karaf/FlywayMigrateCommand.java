/**
 * Copyright 2012 Richemont
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

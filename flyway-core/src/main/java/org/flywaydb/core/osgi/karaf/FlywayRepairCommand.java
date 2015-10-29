/**
 * Copyright 2012 Richemont
 */
package org.flywaydb.core.osgi.karaf;

import org.apache.felix.gogo.commands.Command;
import org.flywaydb.core.Flyway;

@Command(name = FlywayRepairCommand.COMMAND_NAME, scope = "runtime", description = "Repairs the Flyway metadata table.", detailedDescription = "Repairs the Flyway metadata table. This will perform the following actions:"
		+ "\n\ta)  Remove any failed migrations on databases without DDL transactions (User objects left behind must still be cleaned up manually)"
		+ "\n\tb)  Correct wrong checksums")
public class FlywayRepairCommand extends FlywayKarafCommandSupport {

	protected static final String COMMAND_NAME = "repair";

	public FlywayRepairCommand() {
		super(COMMAND_NAME, true);

	}

	@Override
	protected String doExecute(Flyway flyway) {
		flyway.repair();
		return null;
	}

}

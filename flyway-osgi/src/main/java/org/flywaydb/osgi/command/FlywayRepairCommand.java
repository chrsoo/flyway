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
import org.flywaydb.osgi.FlywayService;

@Command(name = FlywayRepairCommand.COMMAND_NAME, scope = "runtime", description = "Repairs the Flyway metadata table.", detailedDescription = "Repairs the Flyway metadata table. This will perform the following actions:"
		+ "\n\ta)  Remove any failed migrations on databases without DDL transactions (User objects left behind must still be cleaned up manually)"
		+ "\n\tb)  Correct wrong checksums")
public class FlywayRepairCommand extends FlywayCommandSupport {

	protected static final String COMMAND_NAME = "repair";

	public FlywayRepairCommand() {
		super(COMMAND_NAME, false);

	}

	@Override
	protected String doExecute(FlywayService flyway) {
		flyway.repair();
		return null;
	}

}

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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@Command(name = FlywayCleanCommand.COMMAND_NAME, scope = "runtime", description = "Stop the bundle and drop all database objects for the name schema.", detailedDescription = "Stops the bundle and drops all objects (tables, views, procedures, triggers, ...) "
		+ "in the configured schemas. The schemas are cleaned in the order "
		+ "specified by the schemas property.")
public class FlywayCleanCommand extends FlywayCommandSupport {

	protected static final String COMMAND_NAME = "clean";

	public FlywayCleanCommand() {
		super(COMMAND_NAME, false);
	}

	@Override
	protected String doExecute(FlywayService flyway) throws Exception {
		BundleContext context = getBundleContext();
		Bundle bundle = context.getBundle(flyway.getBundleId());
		bundle.stop();
		flyway.clean();
		return "bundle " + bundle.getBundleId() + " stopped.";
	}

}

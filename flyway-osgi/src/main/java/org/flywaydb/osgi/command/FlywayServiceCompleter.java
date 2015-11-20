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

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.console.BundleContextAware;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.osgi.FlywayService;
import org.flywaydb.osgi.OsgiFlywayServiceBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


public class FlywayServiceCompleter implements Completer, BundleContextAware {

	private static final Log LOG = LogFactory
			.getLog(FlywayServiceCompleter.class);

	private BundleContext context;

	public FlywayServiceCompleter() {
		// noop
	}

	// -- Completer

	@Override
	public int complete(String buffer, int cursor, List<String> candidates) {

		List<String> migrations = getMigrationNames(context);
		StringsCompleter completer = new StringsCompleter(migrations);

		return completer.complete(buffer, cursor, candidates);
	}

	// -- BundleContextAware

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.context = bundleContext;
	}

	// -- FlywayServiceCompleter

	private List<String> getMigrationNames(BundleContext context) {

		List<String> services = new ArrayList<String>();
		ServiceReference[] references = null;
		try {
			references = context.getServiceReferences(FlywayService.class.getName(), null);
			if (references == null) {
				return services;
			}
			for (ServiceReference reference : references) {
				services.add((String) reference.getProperty(
						OsgiFlywayServiceBean.FLYWAY_OSGI_SERVICE_ID_PROPERTY));
			}
		} catch (InvalidSyntaxException e) {
			LOG.warn("Could not retrieve Flyway Services; " + e.getMessage());
		} finally {
			if(references != null) {
				ungetReferences(references);
			}
		}

		return services;
	}

	private void ungetReferences(ServiceReference[] references) {
		for (ServiceReference reference : references) {
			try {
				context.ungetService(reference);
			} catch (Exception e) {
				LOG.warn(
						"Could not unget service reference; " + e.getMessage());
			}
		}
	}

}

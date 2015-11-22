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
package org.flywaydb.osgi;

import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class OsgiFlywayServiceBean extends Flyway
		implements FlywayService, Comparable<OsgiFlywayServiceBean> {

	public static final String FLYWAY_ACTIONS_PROPERTY = "flyway.actions";

	private static final Log LOG = LogFactory
			.getLog(OsgiFlywayServiceBean.class);

	private final long bundleId;
	private final String name;

	private FlywayAction[] actions;

	/**
	 * OSGI Service Property name used to identify individual Flyway Migrations
	 */
	public static final String FLYWAY_OSGI_SERVICE_ID_PROPERTY = "name";

	// -- Flyway

	@Override
	public void configure(Properties properties) {
		super.configure(properties);
		if (properties.contains(FLYWAY_ACTIONS_PROPERTY)) {
			String value = properties.getProperty(FLYWAY_ACTIONS_PROPERTY);
			String[] actions = StringUtils.tokenizeToStringArray(value, " ,");
			this.actions = new FlywayAction[actions.length];
			for (int i = 0; i < this.actions.length; i++) {
				this.actions[i] = FlywayAction
						.valueOf(actions[i].toUpperCase());
			}
		}
	}

	// -- BundleFlywayServiceBean

	public OsgiFlywayServiceBean(String name, long bundleId) {
		this.name = name;
		this.bundleId = bundleId;
	}

	// -- BundleFlywayService


	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getBundleId() {
		return bundleId;
	}

	@Override
	public void init() {
		if (actions == null) {
			LOG.warn("Initialization not performed as no "
					+ "init actions have been confiugred!");
		} else {
			init(actions);
		}

	}

	@Override
	public void init(FlywayAction[] actions) {

		LOG.info("Initializing Flyway with actions "
				+ StringUtils.arrayToCommaDelimitedString(actions));

		for (FlywayAction action : actions) {
			switch (action) {
			case CLEAN:
				clean();
				break;
			case MIGRATE:
				migrate();
				break;
			case BASELINE:
				baseline();
				break;
			case REPAIR:
				repair();
				break;
			case INFO:
				logInfo();
				break;
			default:
				throw new IllegalArgumentException(
						"Action " + action + " not supported!");
			}
		}
	}

	/**
	 * Logs the current migration info using the underlying logging system.
	 */
	private void logInfo() {
		MigrationInfoService info = info();
		MigrationInfo current = info.current();
		String dump = MigrationInfoDumper
				.dumpToAsciiTable(new MigrationInfo[] { current });
		LOG.info("Current Flyway migration status:\n" + dump);
	}

	public void setActions(FlywayAction[] actions) {
		this.actions = actions;
	}

	// -- Object

	@Override
	public String toString() {
		return name + "(" + bundleId + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (bundleId ^ (bundleId >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OsgiFlywayServiceBean other = (OsgiFlywayServiceBean) obj;
		if (bundleId != other.bundleId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	// -- Comparable

	@Override
	public int compareTo(OsgiFlywayServiceBean o) {
		return o == null ? 1 : name.compareTo(o.getName());
	}

}

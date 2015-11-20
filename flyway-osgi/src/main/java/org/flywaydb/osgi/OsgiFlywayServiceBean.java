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

import org.flywaydb.core.Flyway;

public class OsgiFlywayServiceBean extends Flyway
		implements FlywayService, Comparable<OsgiFlywayServiceBean> {

	private final long bundleId;
	private final String name;

	/**
	 * OSGI Service Property name used to identify individual Flyway Migrations
	 */
	public static final String FLYWAY_OSGI_SERVICE_ID_PROPERTY = "name";

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

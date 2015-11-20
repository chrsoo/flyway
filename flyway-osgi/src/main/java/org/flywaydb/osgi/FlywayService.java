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

import javax.sql.DataSource;

import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;

/**
 * Flyway migrate, baseline, clean, repair and info services.
 */
public interface FlywayService {

	// -- actions
	int migrate();

	void baseline();

	void clean();

	void repair();

	// -- accessors

	MigrationInfoService info();

	String[] getSchemas();

	String getName();

	long getBundleId();

	// -- mutators

	void setBaselineVersion(MigrationVersion version);

	void setBaselineDescription(String description);

	void setDataSource(DataSource dataSource);

}

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
package org.flywaydb.core.osgi;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.osgi.framework.Bundle;

public interface FlywayFactory {

	/**
	 * Create a Flyway instance for the managed configuration of the given name
	 * using the defaultConf as default values. If no managed service
	 * configuration can be found, it is created using the defaultConf as
	 * default.
	 *
	 * @param bundle
	 *            The Bundle of where the Flyway migrations are defined
	 * @param config
	 * 			  Flyway configuration extracted from a Bundle
	 * @return a configured Flyway instance ready to be used
	 * @throws FlywayException
	 *             if the Flyway instance could not be created
	 */
	Flyway create(Bundle bundle, FlywayBundleConfiguration config) throws FlywayException;

}

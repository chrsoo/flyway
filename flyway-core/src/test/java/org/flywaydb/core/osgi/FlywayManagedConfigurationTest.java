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

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

/**
 *
 * @author christoffer.soop
 */
public class FlywayManagedConfigurationTest {

	@Test
	public void test() {
		Properties props = new Properties();
		props.put(FlywayManagedConfiguration.FELIX_FILEINSTALL_FILENAME_PROPERTY,
				"file:/D:/apache-servicemix-5.1.4/etc/org.flywaydb.core-authzdb.cfg");
		FlywayManagedConfiguration flywayConf = new FlywayManagedConfiguration(props);
		assertEquals("authzdb", flywayConf.getName());
	}

}

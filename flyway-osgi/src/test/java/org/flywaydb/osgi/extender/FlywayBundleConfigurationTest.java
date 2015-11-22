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
package org.flywaydb.osgi.extender;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.flywaydb.osgi.extender.FlywayBundleConfiguration;
import org.junit.Before;
import org.junit.Test;

public class FlywayBundleConfigurationTest {

	private FlywayBundleConfiguration conf;

	@Before
	public void setup() throws MalformedURLException {
		URL url = getClass().getResource("authzdb.properties");
		conf = new FlywayBundleConfiguration(url);
		assertNotNull(url);
	}

	@Test
	public void name() {
		assertEquals("authzdb", conf.getName());
	}

	@Test
	public void loadProperties() {
		Properties properties = conf.loadProperties();
		assertEquals("anonymous", properties.get("flyway.username"));
	}

}
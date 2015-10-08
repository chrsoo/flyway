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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.flywaydb.core.Flyway;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Flyway configuration managed by a {@link ManagedServiceFactory}.
 */
public class FlywayManagedConfiguration {

	/**
	 * The name associated with the bundle configuration
	 */
	public static final String FLYWAY_NAME_PROPERTY = Flyway.FLYWAY_PROPERTY_PREFIX + "name";

	public static final String FELIX_FILEINSTALL_FILENAME_PROPERTY =
			"felix.fileinstall.filename";

	private static final Pattern FELIX_FILEINSTALL_FILENAME_PATTERN =
			Pattern.compile("^.*-(\\w+)\\.cfg$");

	private final Properties properties;
	private final String name;

	public FlywayManagedConfiguration(Dictionary<?,?> config) {

		if (config instanceof Properties) {
			this.properties = (Properties) config;
		} else {
			this.properties = new Properties();
			Enumeration<?> enumeration = config.keys();
			while (enumeration.hasMoreElements()) {
				Object key = enumeration.nextElement();
				Object value = config.get(key);
				properties.put(key, value);
			}
		}

		this.name = getFlywayName(properties);

	}

	private String getFlywayName(Properties flywayConf) {
		// Use the flyway.name property, if defined
		String name = get(FLYWAY_NAME_PROPERTY);

		// Fallback to the Felix configuration filename
		if(name == null) {
			name = getFelixName(flywayConf);
		}
		// TODO add fallbacks to other OSGI implementation configuration files

		return name;
	}

	private String getFelixName(Properties flywayConf) {
		String name = get(FELIX_FILEINSTALL_FILENAME_PROPERTY);
		if(name != null) {
			Matcher matcher = FELIX_FILEINSTALL_FILENAME_PATTERN.matcher(name);
			if(matcher.matches()) {
				name = matcher.group(1);
			}
		}
		return name;
	}
	public String getName() {
		return this.name;
	}

	public Properties getProperties() {
		return properties;
	}

	public String get(String key) {
		String value = (String) properties.get(key);
		return value == null
				? null
				: value.trim();
	}

}

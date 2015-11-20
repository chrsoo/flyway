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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class FlywayBundleConfiguration {

	private static final Log LOG = LogFactory.getLog(FlywayBundleConfiguration.class);

	private static final Pattern CONFIGURATIN_NAME_PATTERN =
			Pattern.compile(".*?(\\w+)\\.properties$");

	private final URL url;

	private final String name;

	public FlywayBundleConfiguration(URL url) {

		if(url == null) {
			throw new IllegalArgumentException("The configuration URL cannot be null");
		}

		this.url = url;

		String file = url.getPath();
		Matcher nameMatcher = CONFIGURATIN_NAME_PATTERN.matcher(file);
		if(nameMatcher.matches()) {
			this.name = nameMatcher.group(1);
		} else {
			throw new IllegalArgumentException("The Flyway configuration "
					+ "name cannot be deduced from the configuration URL '" + url + "'");
		}
	}

	public String getName() {
		return name;
	}

	public Properties loadProperties() {
		Properties properties = new Properties();
		InputStream stream = null;
		try {
			stream = url.openStream();
			properties.load(stream);
		} catch (IOException e) {
			throw new FlywayException(
					"Could not open Flyway configuration "
					+ "properties from URL '" + url + "'");
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (Exception e2) {
					LOG.debug("Could not close stream; " + e2.getMessage());
				}
			}
		}

		return properties;
	}

	public URL getUrl() {
		return this.url;
	}

}

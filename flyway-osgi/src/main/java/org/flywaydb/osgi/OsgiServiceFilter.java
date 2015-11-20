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

/**
 * Simple OSGI Filter represents a logical AND between a number of property
 * values.
 */
public class OsgiServiceFilter {

	private final StringBuilder builder = new StringBuilder();

	private int depth = 0;

	private Properties props;

	private OsgiServiceFilter() {
	}

	private OsgiServiceFilter(Properties props) {
		this.props = props;
	}

	public static OsgiServiceFilter with(Properties props) {
		return new OsgiServiceFilter(props);
	}

	public static OsgiServiceFilter create() {
		return new OsgiServiceFilter();
	}

	public OsgiServiceFilter property(String key, String value) {

		key = nullSafeTrime(key);
		value = nullSafeTrime(value);

		builder.append("(")
				.append(key)
				.append("=")
				.append(value)
				.append(")");

		return this;
	}

	public OsgiServiceFilter and() {
		builder.append("(&");
		depth++;
		return this;
	}

	public OsgiServiceFilter or() {
		builder.append("(|");
		depth++;
		return this;
	}

	public OsgiServiceFilter end() {
		builder.append(")");
		depth--;
		return this;
	}

	public OsgiServiceFilter key(String key) {
		return property(key, props.getProperty(key));
	}

	public String build() {
		if (depth != 0) {
			throw new IllegalStateException("Unclosed and/or operand groups");
		}
		return builder.toString();
	}

	public String nullSafeTrime(String value) {
		return value == null
				? null
				: value.trim();
	}
}
package org.flywaydb.core.osgi;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Simple OSGI Filter represents a logical AND between a number of property
 * values.
 */
public class OsgiJdbcServiceFilter {

	private final Properties props = new Properties();

	/**
	 * Create a filter based on a selection of property names. Property names
	 * with null or empty values are ignored.
	 */
	public OsgiJdbcServiceFilter(Properties config, String... properties) {
		for (String property : properties) {
			addPropertyValue(config, property);
		}
	}

	public void addPropertyValue(Properties config, String property) {
		String value = FlywayConfigurationFactory.getConfigValue(config, property);
		if (value != null) {
			props.put(property, value.trim());
		}
	}

	/**
	 * Add an arbitrary property value to the filter configuration
	 */
	public void addProperty(String name, String value) {
		props.put(name, value);
	}

	@Override
	public String toString() {
		StringBuilder builder;
		switch (props.size()) {
		case 0:
			return null;
		case 1:
			builder = new StringBuilder();
			break;
		default:
			builder = new StringBuilder("(&");
			break;
		}

		Object key;
		Enumeration<Object> keys = props.keys();
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			builder.append("(");
			builder.append(key);
			builder.append("=");
			builder.append(props.get(key));
			builder.append(")");
		}

		if (props.size() > 1) {
			builder.append(")");
		}

		return builder.toString();
	}

	public Properties getProperties() {
		return props;
	}

}
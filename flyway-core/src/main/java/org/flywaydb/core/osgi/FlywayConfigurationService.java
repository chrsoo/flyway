/**
 * Copyright 2012 Richemont
 */
package org.flywaydb.core.osgi;

public interface FlywayConfigurationService {
	FlywayConfiguration getConfiguration(String name);
}

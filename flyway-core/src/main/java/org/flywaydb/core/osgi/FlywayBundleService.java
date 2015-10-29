/**
 * Copyright 2012 Richemont
 */
package org.flywaydb.core.osgi;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.osgi.framework.Bundle;

public interface FlywayBundleService {

	/**
	 * Scan the bundle for configurations
	 *
	 * @return list of configurations found in the bundle
	 */
	List<FlywayBundleConfiguration> scan(Bundle bundle);

	/**
	 * Create a Flyway instance for the managed configuration of the given name
	 * using the defaultConf as default values. If no managed service
	 * configuration can be found, it is created using the defaultConf as
	 * default.
	 *
	 * @param bundle
	 *            The Bundle of where the Flyway migrations are defined
	 * @param config
	 *            Flyway configuration extracted from a Bundle
	 * @return a configured Flyway instance ready to be used
	 * @throws FlywayException
	 *             if the Flyway instance could not be created
	 */
	Flyway create(Bundle bundle, FlywayBundleConfiguration config)
			throws FlywayException;

}

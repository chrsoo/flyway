package org.flywaydb.core.osgi;

import java.util.Properties;

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
	 * @param name
	 *            Name of the default Flyway configuration
	 * @param conf
	 *            Flyway configuration
	 * 
	 * @return a configured Flyway instance ready to be used
	 * @throws FlywayException
	 *             if the Flyway instance could not be created
	 */
	Flyway create(Bundle bundle, String name, Properties conf) throws FlywayException;

}

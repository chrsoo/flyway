package org.flywaydb.core.internal.util.scanner;

import org.flywaydb.core.internal.util.Location;

public interface Scanner {

	/**
	 * Scans this location for resources, starting with the specified prefix and ending with the specified suffix.
	 *
	 * @param location The location to start searching. Subdirectories are also searched.
	 * @param prefix   The prefix of the resource names to match.
	 * @param suffix   The suffix of the resource names to match.
	 * @return The resources that were found.
	 */
	Resource[] scanForResources(Location location, String prefix, String suffix);

	/**
	 * Scans the classpath for concrete classes under the specified package implementing this interface.
	 * Non-instantiable abstract classes are filtered out.
	 *
	 * @param location             The location (package) in the classpath to start scanning.
	 *                             Subpackages are also scanned.
	 * @param implementedInterface The interface the matching classes should implement.
	 * @return The non-abstract classes that were found.
	 * @throws Exception when the location could not be scanned.
	 */
	Class<?>[] scanForClasses(Location location, Class<?> implementedInterface) throws Exception;
	
	/**
	 * Load a class that is returned in the {@link #scanForClasses(Location, Class)} method.
	 * @param name of the class
	 * @return the class object represented by the class' name parameter
	 * @throws ClassNotFoundException if the class cannot be loaded
	 */
	Class<?> loadClass(String name) throws ClassNotFoundException;
	
}
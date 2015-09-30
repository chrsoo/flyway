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
package org.flywaydb.core.internal.util.scanner;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.android.AndroidScanner;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathScanner;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemScanner;

/**
 * Scanner for Resources and Classes using a given ClassLoader.
 */
public class DefaultScanner implements Scanner {
	
    private final ClassLoader classLoader;

    /**
     * Create a DefaultScanner for a given Class Loader.
     * 
     * @param classLoader used for scanning for classes and resources
     */
    public DefaultScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.util.scanner.Scanner#scanForResources(org.flywaydb.core.internal.util.Location, java.lang.String, java.lang.String)
	 */
    @Override
	public Resource[] scanForResources(Location location, String prefix, String suffix) {
        try {
            if (location.isFileSystem()) {
                return new FileSystemScanner().scanForResources(location.getPath(), prefix, suffix);
            }

            if (new FeatureDetector(classLoader).isAndroidAvailable()) {
                return new AndroidScanner(classLoader).scanForResources(location.getPath(), prefix, suffix);
            }

            return new ClassPathScanner(classLoader).scanForResources(location.getPath(), prefix, suffix);
        } catch (Exception e) {
            throw new FlywayException("Unable to scan for SQL migrations in location: " + location, e);
        }
    }


    /* (non-Javadoc)
	 * @see org.flywaydb.core.internal.util.scanner.Scanner#scanForClasses(org.flywaydb.core.internal.util.Location, java.lang.Class)
	 */
    @Override
	public Class<?>[] scanForClasses(Location location, Class<?> implementedInterface) throws Exception {
        if (new FeatureDetector(classLoader).isAndroidAvailable()) {
            return new AndroidScanner(classLoader).scanForClasses(location.getPath(), implementedInterface);
        }

        return new ClassPathScanner(classLoader).scanForClasses(location.getPath(), implementedInterface);
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    	return classLoader.loadClass(name);
    }
    
    public ClassLoader getClassLoader() {
    	return classLoader;
    }
}
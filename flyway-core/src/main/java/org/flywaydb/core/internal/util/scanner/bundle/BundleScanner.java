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
package org.flywaydb.core.internal.util.scanner.bundle;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.osgi.framework.Bundle;

public class BundleScanner implements Scanner {

	private static final Log LOG = LogFactory.getLog(BundleScanner.class);

	private final Bundle bundle;

	public BundleScanner(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Resource[] scanForResources(Location location, String prefix, String suffix) {
        Set<Resource> resourceNames = new TreeSet<Resource>();

        String filePattern = trimToEmpty(prefix) + "*" + trimToEmpty(suffix);

        @SuppressWarnings({"unchecked"})
        Enumeration<URL> entries = bundle.findEntries(location.getPath(), filePattern, true);

        if (entries != null) {
            while (entries.hasMoreElements()) {
                URL entry = entries.nextElement();

		        String path = entry.getPath();
                final String resourceName = path.startsWith("/")
                		? path.substring(1)
                		: path;

                resourceNames.add(new BundleResource(bundle, resourceName));
            }
        }

        return resourceNames.toArray(new Resource[resourceNames.size()]);
	}

	@Override
	public Class<?>[] scanForClasses(Location location, Class<?> implementedInterface) throws Exception {
        LOG.debug("Scanning for classes at '" + location + "' (Implementing: '" + implementedInterface.getName() + "')");

        List<Class<?>> classes = new ArrayList<Class<?>>();

        Resource[] resources = scanForResources(location, "", ".class");
        for (Resource resource : resources) {
            String className = toClassName(resource.getLocation());
            Class<?> clazz = bundle.loadClass(className);

            if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum() || clazz.isAnonymousClass()) {
                LOG.debug("Skipping non-instantiable class: " + className);
                continue;
            }

            if (!implementedInterface.isAssignableFrom(clazz)) {
                continue;
            }

            try {
                clazz.newInstance();
            } catch (Exception e) {
                throw new FlywayException("Unable to instantiate class: " + className, e);
            }

            classes.add(clazz);
            LOG.debug("Found class: " + className);
        }

        return classes.toArray(new Class<?>[classes.size()]);
	}

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    	return bundle.loadClass(name);
    }

    // -- BundleScanner

    /**
     * Converts this resource name to a fully qualified class name.
     *
     * @param resourceName The resource name.
     * @return The class name.
     */
    private String toClassName(String resourceName) {
    	String nameWithDots = resourceName.replace("/", ".");
    	return nameWithDots.substring(0, (nameWithDots.length() - ".class".length()));
    }

    public String trimToEmpty(String value) {
    	return value == null ? "" : value.trim();
    }

}

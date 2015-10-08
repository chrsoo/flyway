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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.osgi.framework.Bundle;

/**
 * The BundleResource borrows heavily from the {@link ClassPathResource} implementation
 */
final class BundleResource implements Comparable<BundleResource>, Resource {

	private final Bundle bundle;
	private final String location;

	BundleResource(Bundle bundle, String resourceName) {
		this.bundle = bundle;
		this.location = resourceName;
	}

	@Override
	public String loadAsString(String encoding) {
		URL resource = bundle.getResource(location);
	    try {
	        InputStream inputStream = resource.openStream();
	        if (inputStream == null) {
	            throw new FlywayException("Unable to obtain "
	            		+ "inputstream for resource: " + location);
	        }
	        Reader reader = new InputStreamReader(inputStream,
	        		Charset.forName(encoding));

	        return FileCopyUtils.copyToString(reader);
	    } catch (IOException e) {
	        throw new FlywayException("Unable to load resource: " +
	        		location + " (encoding: " + encoding + ")", e);
	    }
	}

	@Override
	public byte[] loadAsBytes() {
		URL resource = getUrl(location);
	    try {
	        InputStream inputStream = resource.openStream();
	        return FileCopyUtils.copyToByteArray(inputStream);
	    } catch (IOException e) {
	        throw new FlywayException("Unable to load resource: " + location, e);
	    }
	}

	@Override
	public String getLocationOnDisk() {
		URL url = getUrl(location);
	    try {
	        return new File(URLDecoder.decode(url.getPath(), "UTF-8")).getAbsolutePath();
	    } catch (UnsupportedEncodingException e) {
	        throw new FlywayException("Unknown encoding: UTF-8", e);
	    }
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public String getFilename() {
		return location.contains("/")
				? location.substring(location.lastIndexOf("/") + 1)
				: location;
	}

	private URL getUrl(final String resource) {
		URL url = bundle.getResource(resource);
		if (resource == null) {
			throw new FlywayException("Unable to retrieve resource '" + resource + "'");
		}
		return url;
	}

	// -- Comparable<BundleResource>

    public int compareTo(BundleResource o) {
        return location.compareTo(o.location);
    }

	// -- Object

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BundleResource that = (BundleResource) o;

        if (!location.equals(that.location)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

}
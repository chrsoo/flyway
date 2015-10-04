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
import org.osgi.framework.Bundle;

/**
 * The BundleResource borrows heavily from the ClassPathResource implementation
 *
 */
final class BundleResource implements Resource {
	
	private final Bundle bundle;
	private final String resourceName;

	BundleResource(Bundle bundle, String resourceName) {
		this.bundle = bundle;
		this.resourceName = resourceName;
	}

	@Override
	public String loadAsString(String encoding) {
		URL resource = bundle.getResource(resourceName);
	    try {
	        InputStream inputStream = resource.openStream();
	        if (inputStream == null) {
	            throw new FlywayException("Unable to obtain "
	            		+ "inputstream for resource: " + resourceName);
	        }
	        Reader reader = new InputStreamReader(inputStream, 
	        		Charset.forName(encoding));

	        return FileCopyUtils.copyToString(reader);
	    } catch (IOException e) {
	        throw new FlywayException("Unable to load resource: " + 
	        		resourceName + " (encoding: " + encoding + ")", e);
	    }
	}

	@Override
	public byte[] loadAsBytes() {
		URL resource = getUrl(resourceName);						
	    try {
	        InputStream inputStream = resource.openStream();
	        return FileCopyUtils.copyToByteArray(inputStream);
	    } catch (IOException e) {
	        throw new FlywayException("Unable to load resource: " + resourceName, e);
	    }
	}

	@Override
	public String getLocationOnDisk() {
		URL url = getUrl(resourceName);
	    try {
	        return new File(URLDecoder.decode(url.getPath(), "UTF-8")).getAbsolutePath();
	    } catch (UnsupportedEncodingException e) {
	        throw new FlywayException("Unknown encoding: UTF-8", e);
	    }
	}

	@Override
	public String getLocation() {
		return resourceName;
	}

	@Override
	public String getFilename() {
		return resourceName.contains("/") 
				? resourceName.substring(resourceName.lastIndexOf("/") + 1)
				: resourceName;
	}

	private URL getUrl(final String resource) {
		URL url = bundle.getResource(resource);
		if (resource == null) {
			throw new FlywayException("Unable to retrieve resource '" + resource + "'");
		}
		return url;
	}
}
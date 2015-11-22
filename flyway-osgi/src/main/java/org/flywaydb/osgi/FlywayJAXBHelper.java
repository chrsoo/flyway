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

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.w3c.dom.Element;

public class FlywayJAXBHelper {

	private static final Log LOG = LogFactory.getLog(FlywayJAXBHelper.class);

	protected final JAXBContext context;

	public FlywayJAXBHelper(Class<?>... classes) {
		try {
			context = JAXBContext.newInstance(classes);
		} catch (JAXBException e) {
			String msg = "Could not create JAXB Context";
			LOG.warn(msg);
			throw new IllegalStateException(msg, e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T unmarshal(Element node, Class<T> type) {
		try {
			Unmarshaller unmarshaller = context.createUnmarshaller();
			T jaxb = (T) unmarshaller.unmarshal(node);

			return jaxb;

		} catch (JAXBException e) {
			String msg = "Could not parse node";
			LOG.warn(msg);
			throw new IllegalStateException(msg, e);
		}
	}

	public String marshal(Object jaxb, boolean pretty) {
		try {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, pretty);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal(jaxb, baos);
			return baos.toString();
		} catch (PropertyException e) {
			throw new IllegalStateException("Could not configure marshaller for formatted output", e);
		} catch (JAXBException e) {
			throw new RuntimeException("Could not print XML", e);
		}
	}

}

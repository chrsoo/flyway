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

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutablePassThroughMetadata;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FlywayNamespaceHandler implements NamespaceHandler {

	public static final String FLYWAY_SCHEMA_LOCATION = "/META-INF/schema/flyway-blueprint-1.0.xsd";
	public static final String FLYWAY_NS = "http://flywaydb.org/xmlns/flyway-blueprint";

	private static final Log LOG = LogFactory
			.getLog(FlywayNamespaceHandler.class);

	private final FlywayJAXBHelper parser = new FlywayJAXBHelper(
			FlywayServiceJAXB.class);
	private final FlywayServiceRegistry flywayServiceRegistry;

	// -- NamespaceHandler

	public FlywayNamespaceHandler(FlywayServiceRegistry flywayServiceFactory) {
		this.flywayServiceRegistry = flywayServiceFactory;
	}

	@Override
	public ComponentMetadata decorate(Node node, ComponentMetadata metadata,
			ParserContext context) {

		LOG.warn("Ignoring uhandled node '" + node.getLocalName() + "'");
		return metadata;

	}

	@Override
	public Metadata parse(Element element, ParserContext context) {

		if (element.getLocalName().equals(FlywayServiceJAXB.ROOT_ELEMENT)) {
			return createFlywayService(context, element);
		} else {
			LOG.warn("Ignoring unhandled element '" + element.getLocalName()
					+ "'");
			return null;
		}

	}

	@Override
	public URL getSchemaLocation(String namespace) {
		return FLYWAY_NS.equals(namespace)
				? this.getClass().getResource(FLYWAY_SCHEMA_LOCATION)
				: null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Set<Class> getManagedClasses() {
		return null;
	}

	// -- FlywayNamespaceHandler

	/**
	 * Create component metadata for the Flyway Service element
	 */
	private Metadata createFlywayService(ParserContext context,
			Element element) {

		FlywayServiceJAXB serviceJAXB = parser.unmarshal(element,
				FlywayServiceJAXB.class);

		LOG.debug("Creating Flyway Service Blueprint component metadata "
				+ "for the name " + serviceJAXB.getName() + " Flyway Service");

		MutablePassThroughMetadata metadata = context.createMetadata(
				MutablePassThroughMetadata.class);

		metadata.setId(serviceJAXB.getId());
		FlywayService service = getFlywayService(context,
				serviceJAXB.getName());

		service.init(serviceJAXB.getActions());

		metadata.setObject(service);
		return metadata;

	}

	// FIXME use get instead of find to get the FlywayService!
	private FlywayService getFlywayService(ParserContext context, String name) {
		// If we can find a way to get the bundle ID when parsing the Blueprint
		// Context we should use flywayServiceRegistry.get(bundleId, name)
		// instead!
		List<FlywayService> services = flywayServiceRegistry.findByName(name);
		switch(services.size()) {
		case 1:
			return services.get(0);
		case 0:
			throw new RuntimeException("No FlywayService with the name '" + name + "'");
		default:
			throw new RuntimeException(
					"More than one Flyway Service with the name '" + name
							+ "' found, please consider changing the name!");
		}
	}

}
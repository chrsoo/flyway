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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = FlywayServiceJAXB.ROOT_ELEMENT)
public final class FlywayServiceJAXB {

	public static final String ROOT_ELEMENT = "service";

	private static final FlywayAction[] DEFAULT_ACTIONS = new FlywayAction[] {
			FlywayAction.MIGRATE };

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String name;

	@XmlAttribute(name = "on-init")
	private FlywayAction[] actions = DEFAULT_ACTIONS;

	protected FlywayServiceJAXB() {
		// no-arg JAXB constructor
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name == null ? id : name;
	}

	public FlywayAction[] getActions() {
		return actions;
	}

}

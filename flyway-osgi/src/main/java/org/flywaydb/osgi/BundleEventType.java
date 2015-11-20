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

import org.osgi.framework.BundleEvent;

/**
 * Enum representing the BundleEventTypes
 */
public enum BundleEventType {
	//@formatter:off
	INSTALLED(BundleEvent.INSTALLED),
	RESOLVED(BundleEvent.RESOLVED),
	LAZY_ACTIVATION(BundleEvent.LAZY_ACTIVATION),
	STARTING(BundleEvent.STARTING),
	STARTED(BundleEvent.STARTED),
	STOPPING(BundleEvent.STOPPING),
	STOPPED(BundleEvent.STOPPED),
	UPDATED(BundleEvent.UPDATED),
	UNRESOLVED(BundleEvent.UNRESOLVED),
	UNINSTALLED(BundleEvent.UNINSTALLED),
	NULL(-1)
	;
	//@formatter:on
	private final int code;

	private BundleEventType(int code) {
		this.code = code;
	}

	/**
	 * Null-safe get the event type for a given BundleEvent.
	 * @param event the event, may be null
	 * @return The event's type or <code>null</code>
	 */
	public static BundleEventType get(BundleEvent event) {

		if(event == null) {
			return NULL;
		}

		return get(event.getType());
	}

	/**
	 * Null-safe get the event type for a given BundleEvent.
	 * @param event the event, may be null
	 * @return The event's type or <code>null</code>
	 */
	public static BundleEventType get(int code) {

		for(BundleEventType type : BundleEventType.values()) {
			if(type.code == code) {
				return type;
			}
		}

		throw new IllegalStateException("The BundleEventType for code " + code + "' is unknown!");
	}

	public static int getMask(BundleEventType... types) {
		int mask = 0;
		for(BundleEventType type : types) {
			mask |= type.code;
		}
		return mask;
	}

	public static boolean matches(BundleEventType type, int mask) {
		return (mask & type.code) > 0;
	}

	public int getCode() {
		return code;
	}
}
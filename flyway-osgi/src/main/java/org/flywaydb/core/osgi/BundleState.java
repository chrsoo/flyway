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
package org.flywaydb.core.osgi;

import org.osgi.framework.Bundle;

/**
 * Enum reprsenting the Bundle States.
 */
public enum BundleState {

	UNINSTALLED(Bundle.UNINSTALLED),
	INSTALLED(Bundle.INSTALLED),
	RESOLVED(Bundle.RESOLVED),
	STARTING(Bundle.STARTING),
	STOPPING(Bundle.STOPPING),
	ACTIVE(Bundle.ACTIVE)
	;

	private final int state;

	private BundleState(int state) {
		this.state = state;
	}

	public static BundleState get(int state) {
		for(BundleState bundleState : BundleState.values()) {
			if(bundleState.state == state) {
				return bundleState;
			}
		}
		throw new IllegalStateException("The BundleState " + state + "' us unknown!");
	}

	public static int getMask(BundleState... states) {
		int mask = 0;
		for(BundleState bundleState : states) {
			mask |= bundleState.state;
		}
		return mask;
	}

	public static boolean matches(BundleState bundleState, int mask) {
		return (mask & bundleState.state) > 0;
	}
}

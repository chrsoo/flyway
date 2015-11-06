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

import static org.junit.Assert.*;

import org.junit.Test;
import org.osgi.framework.Bundle;

public class BundleStateTest {

	@Test
	public void getBundleState() {
		assertEquals(BundleState.INSTALLED, BundleState.get(Bundle.INSTALLED));
		assertEquals(BundleState.RESOLVED, BundleState.get(Bundle.RESOLVED));
		assertEquals(BundleState.STARTING, BundleState.get(Bundle.STARTING));
		assertEquals(BundleState.STOPPING, BundleState.get(Bundle.STOPPING));
		assertEquals(BundleState.ACTIVE, BundleState.get(Bundle.ACTIVE));
		assertEquals(BundleState.UNINSTALLED, BundleState.get(Bundle.UNINSTALLED));
	}

	@Test
	public void mask() {

		int mask = BundleState.getMask(BundleState.INSTALLED, BundleState.RESOLVED);

		assertTrue(BundleState.matches(BundleState.INSTALLED, mask));
		assertTrue(BundleState.matches(BundleState.RESOLVED, mask));
		assertFalse(BundleState.matches(BundleState.STARTING, mask));
		assertFalse(BundleState.matches(BundleState.STOPPING, mask));
		assertFalse(BundleState.matches(BundleState.ACTIVE, mask));
		assertFalse(BundleState.matches(BundleState.UNINSTALLED, mask));
	}

	@Test(expected=IllegalStateException.class)
	public void illegalBundleState() {
		BundleState.get(10);
	}

}

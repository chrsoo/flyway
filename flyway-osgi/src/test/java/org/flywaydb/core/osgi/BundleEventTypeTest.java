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
import org.osgi.framework.BundleEvent;

public class BundleEventTypeTest {

	@Test
	public void getBundleEventType() {
		assertEquals(BundleEventType.INSTALLED, BundleEventType.get(BundleEvent.INSTALLED));
		assertEquals(BundleEventType.RESOLVED, BundleEventType.get(BundleEvent.RESOLVED));
		assertEquals(BundleEventType.LAZY_ACTIVATION, BundleEventType.get(BundleEvent.LAZY_ACTIVATION));
		assertEquals(BundleEventType.STARTING, BundleEventType.get(BundleEvent.STARTING));
		assertEquals(BundleEventType.STARTED, BundleEventType.get(BundleEvent.STARTED));
		assertEquals(BundleEventType.STOPPING, BundleEventType.get(BundleEvent.STOPPING));
		assertEquals(BundleEventType.STOPPED, BundleEventType.get(BundleEvent.STOPPED));
		assertEquals(BundleEventType.UPDATED, BundleEventType.get(BundleEvent.UPDATED));
		assertEquals(BundleEventType.UNRESOLVED, BundleEventType.get(BundleEvent.UNRESOLVED));
		assertEquals(BundleEventType.UNINSTALLED, BundleEventType.get(BundleEvent.UNINSTALLED));
	}

	@Test
	public void mask() {

		int mask = BundleEventType.getMask(BundleEventType.INSTALLED,
				BundleEventType.RESOLVED, BundleEventType.STOPPING);

		assertTrue(BundleEventType.matches(BundleEventType.INSTALLED, mask));
		assertTrue(BundleEventType.matches(BundleEventType.RESOLVED, mask));
		assertTrue(BundleEventType.matches(BundleEventType.STOPPING, mask));
		assertFalse(BundleEventType.matches(BundleEventType.LAZY_ACTIVATION, mask));
		assertFalse(BundleEventType.matches(BundleEventType.STARTING, mask));
		assertFalse(BundleEventType.matches(BundleEventType.STARTED, mask));
		assertFalse(BundleEventType.matches(BundleEventType.STOPPED, mask));
		assertFalse(BundleEventType.matches(BundleEventType.UPDATED, mask));
		assertFalse(BundleEventType.matches(BundleEventType.UNRESOLVED, mask));
		assertFalse(BundleEventType.matches(BundleEventType.UNINSTALLED, mask));
	}

	@Test(expected=IllegalStateException.class)
	public void illegalBundleEventType() {
		BundleEventType.get(666);
	}

}

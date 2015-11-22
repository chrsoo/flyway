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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class FlywayServiceRegistryBean implements FlywayServiceRegistry {

	private final SortedSet<FlywayService> registry = Collections
			.synchronizedSortedSet(new TreeSet<FlywayService>());

	@Override
	public void register(List<FlywayService> services) {
		registry.addAll(services);
	}

	@Override
	public void unregister(List<FlywayService> services) {
		registry.removeAll(services);
	}

	@Override
	public List<FlywayService> list() {
		ArrayList<FlywayService> copy;
		synchronized (registry) {
			copy = new ArrayList<FlywayService>(
					registry.size());
			for (FlywayService service : registry) {
				copy.add(service);
			}
		}
		return copy;
	}

	@Override
	public FlywayService getService(long bundleId, String name) {

		synchronized (registry) {
			for (FlywayService service : registry) {
				if (service.getBundleId() == bundleId
						&& service.getName().equals(name)) {
					return service;
				}
			}
		}

		return null;
	}

	@Override
	public List<FlywayService> findByBundle(long bundleId) {

		List<FlywayService> services = new ArrayList<FlywayService>();

		synchronized (registry) {
			for (FlywayService service : registry) {
				if (service.getBundleId() == bundleId) {
					services.add(service);
				}
			}
		}

		return services;
	}

	@Override
	public List<FlywayService> findByName(String name) {

		List<FlywayService> services = new ArrayList<FlywayService>();

		synchronized (registry) {
			for (FlywayService service : registry) {
				if (service.getName().equals(name)) {
					services.add(service);
				}
			}
		}

		return services;
	}

}

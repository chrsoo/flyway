/**
 * Copyright 2012 Richemont
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

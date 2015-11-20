/**
 * Copyright 2012 Richemont
 */
package org.flywaydb.osgi;

import java.util.List;

public interface FlywayServiceRegistry {

	void register(List<FlywayService> services);

	void unregister(List<FlywayService> services);

	FlywayService getService(long bundleId, String name);

	List<FlywayService> list();

	List<FlywayService> findByBundle(long bundleId);

	List<FlywayService> findByName(String name);

}

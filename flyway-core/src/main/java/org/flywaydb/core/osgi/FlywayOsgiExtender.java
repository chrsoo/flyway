package org.flywaydb.core.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;

public class FlywayOsgiExtender implements BundleTrackerCustomizer {

	private static final Log LOG = LogFactory.getLog(FlywayOsgiExtender.class);

	private final FlywayFactory factory;

	public FlywayOsgiExtender(FlywayFactory factory) {
		this.factory = factory;
	}

	// -- BundleTrackerCustomizer

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		switch (event.getType()) {
		case Bundle.STARTING:
			migrate(bundle);
			break;
		default:
			LOG.warn("Received unhandled event type " + event.getType());
		}

		return null;
	}

	@Override
	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
		// noop
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		// noop
	}

	// -- FlywayOsgiExtender

	@SuppressWarnings("unchecked")
	public void migrate(Bundle bundle) {
		Enumeration<URL> entries = bundle.findEntries("META-INF/flyway", "*.conf", false);
		URL configUrl;
		while (entries.hasMoreElements()) {
			configUrl = entries.nextElement();
			LOG.debug("Found Flyway configuration URL '" + configUrl + "'");
			migrate(bundle, configUrl);
		}
	}

	private void migrate(Bundle bundle, URL configUrl) {

		String name = getName(configUrl);
		LOG.debug("Migrating '" + name + "'");

		Properties conf = loadProperties(configUrl);
		Flyway flyway = factory.create(bundle, name, conf);

		flyway.migrate();

		LOG.info("Migrated '" + name + "'");
	}

	private String getName(URL configUrl) {
		String file = configUrl.getFile();
		return file.substring(0, file.lastIndexOf("."));
	}

	private Properties loadProperties(URL configUrl) {
		Properties properties = new Properties();
		InputStream stream;
		try {
			stream = configUrl.openStream();
			properties.load(stream);
		} catch (IOException e) {
			throw new FlywayException(
					"Could not open Flyway configuration " + "properties from URL '" + configUrl + "'");
		}

		return properties;
	}

}

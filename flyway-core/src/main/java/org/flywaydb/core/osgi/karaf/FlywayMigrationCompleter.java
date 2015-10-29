/**
 * Copyright 2012 Richemont
 */
package org.flywaydb.core.osgi.karaf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.ArgumentCompleter;
import org.apache.karaf.shell.console.completer.ArgumentCompleter.ArgumentList;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.apache.karaf.shell.console.jline.CommandSessionHolder;
import org.flywaydb.core.osgi.FlywayBundleConfiguration;
import org.flywaydb.core.osgi.FlywayBundleService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import jline.internal.Log;

public class FlywayMigrationCompleter implements Completer {

	private FlywayBundleService service;
	private CommandSession session;
	private BundleContext context;

	@Override
	public int complete(String buffer, int cursor, List<String> candidates) {

		long id = getBundleId();
		if (id == -1) {
			return -1;
		}

		Bundle bundle = context.getBundle(id);
		if (bundle == null) {
			return -1;
		}

		List<FlywayBundleConfiguration> configurations = service.scan(bundle);
		List<String> migrations = new ArrayList<String>(configurations.size());
		for (FlywayBundleConfiguration conf : configurations) {
			migrations.add(conf.getName());
		}

		Collections.sort(migrations);
		StringsCompleter completer = new StringsCompleter(migrations);

		return completer.complete(buffer, cursor, candidates);
	}

	private long getBundleId() {
		session = CommandSessionHolder.getSession();
		if (session == null) {
			return -1;
		}

		ArgumentList list = (ArgumentList) session
				.get(ArgumentCompleter.ARGUMENTS_LIST);

		if (list == null) {
			return -1;
		}

		String[] arguments = list.getArguments();
		if (arguments == null || arguments.length == 0) {
			return -1;
		}

		try {
			return Long.parseLong(arguments[0]);
		} catch (Exception e) {
			Log.debug("Could not parse bundle-id argument", e.getMessage());
			return -1;
		}
	}

	// -- properties

	public FlywayBundleService getFlywayBundleService() {
		return this.service;
	}

	public void setFlywayBundleService(FlywayBundleService service) {
		this.service = service;
	}

	public BundleContext getBundleContext() {
		return this.context;
	}

	public void setBundleContext(BundleContext context) {
		this.context = context;
	}

}

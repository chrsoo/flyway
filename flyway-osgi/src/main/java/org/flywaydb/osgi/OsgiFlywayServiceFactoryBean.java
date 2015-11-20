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

import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.osgi.configuration.FlywayConfiguration;
import org.flywaydb.osgi.configuration.FlywayConfigurationService;
import org.osgi.framework.Bundle;

public class OsgiFlywayServiceFactoryBean implements OsgiFlywayServiceFactory {

	private static final Log LOG = LogFactory
			.getLog(OsgiFlywayServiceFactoryBean.class);

	private FlywayConfigurationService configService;
	private OsgiDataSourceFactory dataSourceFactory;

	public OsgiFlywayServiceFactoryBean(
			FlywayConfigurationService configService,
			OsgiDataSourceFactory dataSourceFactory) {
		this.configService = configService;
		this.dataSourceFactory = dataSourceFactory;
	}

	@Override
	public FlywayService create(Bundle bundle, String migration,
			Properties defaults) {
		return create(bundle, migration, defaults, null);
	}

	@Override
	public FlywayService create(Bundle bundle, String name,
			Properties defaults, DataSource dataSource) {

		LOG.debug("Creating Flyway instance for bundle " + bundle.getBundleId()
				+ " and '" + name + "'");


		OsgiFlywayServiceBean flyway = new OsgiFlywayServiceBean(name,
				bundle.getBundleId());

		// create a new properties instance so as not to modify the defaultConf
		Properties flywayProperties = new Properties();
		flywayProperties.putAll(defaults);

		FlywayConfiguration config = configService.getConfiguration(name);
		if (config == null) {
			LOG.warn("Could not find managed configuration for '" + name + "'");
		} else {
			flywayProperties.putAll(config.getProperties());
		}

		// create the DataSource before we remove the JDBC driver and URL
		if (dataSource == null) {
			dataSource = dataSourceFactory.createDataSource(bundle,
					flywayProperties);
		}
		flyway.setDataSource(dataSource);

		// apply the properties to the Flyway instance
		flyway.configure(flywayProperties);

		// set the Scanner strategy
		BundleScanner scanner = new BundleScanner(bundle);
		flyway.setScanner(scanner);

		return flyway;
	}

}

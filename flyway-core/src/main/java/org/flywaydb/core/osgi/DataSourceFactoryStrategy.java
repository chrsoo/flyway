/**
 * Copyright 2012 Richemont
 */
package org.flywaydb.core.osgi;

import java.util.Properties;

import javax.sql.DataSource;

import org.osgi.framework.Bundle;

public interface DataSourceFactoryStrategy {
	DataSource createDataSource(Bundle bundle, Properties flywayProperties);
}

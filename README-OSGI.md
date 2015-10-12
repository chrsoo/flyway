There are currently two ways to integrate Flyway with your OSGI application:

* Making your bundle a Flyway fragment
* Using the Flyway OSGI Extender

# Flyway Fragment
By making the your bundle a fragment of the `org.flywaydb.core` package Flyway will share the classoader of the fragment and thus gain access to resources contained in your bundle. This means that Flyway's default classpath scanning mechanism can find your Flyway migration scripts and classes.

Fragments do not follow the normal bundle lifecycle and may not be started using an `Bundle-Activator`. This means that Flyway migration scripts and service implementations need to reside in separate bundles.

Although fragments can be made to work using the extender pattern is probably easier and offers a OSGI friendly approach to using Flyway.

# Flyway Extender
The Flyway Extender is a service that will scan all bundles added to the OSGI container for Flyway configurations. When a Flyway configuration is found it is matched with a Managed Service Factory configuration and the migration is executed.

## Usecase
1. The `flyway-core` bundle is installed and started in the OSGI container
1. OSGI Bundles that need flyway support adds a Flyway configuration file in

    ````
    META-INF/flyway/<name>.properties
    ````

1. When the bundle deployed in OSGI and enters the Starting state, `flyway-core` will scan the bundle for all property file resources in `META-INF/flyway`
1. For each property file found it will lookup the corresponding Flyway `name` Managed Service Configuration
   1. If the a Managed Service Configuration cannot be found a new Managed Service Configuration is created using the content of the found property file resource as default
   1. If a Managed Service Flyway Configuration is found it is merged with the property file resource found in the bundle; the Managed Service Flyway Configuration overrides properties found in the property file.
1. From the merged (or default) Flyway configuration a JDBC Driver instance is created using the OSGI Compendium JDBC Service
1. Flyway loads the JDBC driver using the Bundle's classloader. If the OSGI Compendium JDBC Service is available it used in preference to the Bundle's classloader.
1. The driver is configured with all configuration properties fromt the merged Flyway configuration that are not prefixed with `flyway.`
1. The driver is used to create an instance of Flyway's default `DriverDataSource`
1. A `Flyway` instance is created and configured with the merged configuration
1. Migration is done using the Flyway instance that will scann the bundle for migrations

## Managed Service Flyway Configurations
As all resource files in the bundles represent static and default Flyway configurations the an OSGI Managed Service Factory is used to provide environment specific runtime configurations for Flyway. Typically such configurations would contain at least the JDBC URL, username and password.

Managed Service configurations use the factory pid `org.flywaydb.datasource` and individual configurations can thus be defined in the configuration files on the form

````
org.flywaydb.core-<name>.cfg
````

# Example

Given a bundle `security-authz` handling Authorizations in an AuthzDB flyway the following resources are found in the bundle:
````
META-INF
  flyway
    db/migration
       V1__Initial_structure.sql
       V1_1__Populate_table.sql
    authz.properites
````
The file resource `authz.properties` contains
````properties
flyway.driver = com.mysql.jdbc.Driver
flyway.user = authz_user
flyway.password = CHANGEME
flyway.url = jdbc:mysql://localhost:3306/AuthzDB
flyway.locations = META-INF/flyway/db/migration
````
The Flyway Managed Service Configuration for the AuthzDB is found in
````
${etc}/com.flywaydb.core-authzdb.cfg
````
... and it overrides the JDBC url and password:
````
flyway.password = secret
flyway.url = jdbc:mysql://authzdb.internal.acme.com:3306/AuthzDB
````

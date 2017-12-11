# Tombolo Digital Connector
[![wercker status](https://app.wercker.com/status/2279bdc90688501386b12c693be6a186/s/master "wercker status")](https://app.wercker.com/project/byKey/2279bdc90688501386b12c693be6a186)

The [Tombolo Digital Connector](http://www.tombolo.org.uk/products/) is an open source tool that allows data enthusiasts to efficiently connect different data sets into a common format. It enables the **transparent** and **reproducible** combination of data which exists in different domains, different formats and on different spatio-temporal scales. The Tombolo Digital Connector makes it easier to generate models, indexes and insights that rely on the combination of data from different sources.

There are three particularly important parts to the Tombolo Digital Connector: 

- Importers
  - Built-in importers harvest a range of data sources into the centralised data format. Examples include data from ONS, OpenStreetMap, NOMIS, the London Air Quality Network and the London Data Store. **We welcome the creation of additional importers**.
- Centralised data format
  - All data imported into the Tombolo Digital Connector adopts the centralised data format. This makes it easier to combine and modify data from different sources.
- Recipes
  - Users generate recipes with a declarative 'recipe language' to combine the data in different ways. This combination can generate new models, indexes and insights. For example, [existing recipes](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/resources/executions/examples) can generate models of social isolation, calculate the proportion of an area covered by greenspace and even generate an active transport index. **We welcome the creation of additional recipes**.

For further information see the [wiki](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/wiki).


## Table of Contents:

* [Quick start](#quick-start)
* [Continuous Integration](#continuous-integration)
* [Local Deploy](#local-deploy)
* [Run Tasks](#run-tasks)

<p align="center">
  <img src="/readmeresources/dc_animation.gif?raw=true" alt="DigitalConnectorGif"/>
</p>

## Quick start

### Requirements
* [JDK (1.8+)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [PostgreSQL (9.4+)](https://www.postgresql.org/)
* [PostGIS (2.1+)](http://postgis.net/)
* [Gradle (2.12+)](https://gradle.org/)
* (Optional) [Wercker (1.0+)](http://www.wercker.com/)

### Configure the project

Copy and amend the example configuration file at
`/gradle.properties.example` to
`/gradle.properties`.

Copy and amend the example API keys file at
`/apikeys.properties.example` to
`/apikeys.properties`. If you're not using the services mentioned in the file you can leave it as-is.

### Set up main database

Then run the following to set up your database:

```bash
# Create a user and database
createuser tombolo
createdb -O tombolo tombolo -E UTF8
psql -d tombolo -c "CREATE EXTENSION postgis;"
psql -d tombolo -c "SET NAMES 'UTF8';"


# Create DB tables and load initial fixtures
psql -d tombolo -U tombolo < src/main/resources/sql/create_database.sql
```

### Set up test database

The test database is used by the tests and is cleared routinely. We use this
to gain control over what is in the database when our tests are running and
to avoid affecting any important data in your main database.

To set up the test user and database:

```bash
# Create a user and database
createuser tombolo_test
createdb -O tombolo_test tombolo_test -E UTF8
psql -d tombolo_test -c "CREATE EXTENSION postgis;"
psql -d tombolo_test -c "SET NAMES 'UTF8';"

# Create DB tables and load initial fixtures
psql -d tombolo_test -U tombolo_test < src/main/resources/sql/create_database.sql
```

### Run tests

```bash
gradle test
```

If you use the IntelliJ JUnit test runner, you will need to add the following to your
VM Options in your JUnit configuration (Run -> Edit Configurations -> All under JUnit,
and Defaults -> JUnit):

```
-enableassertions
-disableassertions:org.geotools...
-Denvironment=test
-DdatabaseURI=jdbc:postgresql://localhost:5432/tombolo_test
-DdatabaseUsername=tombolo_test
-DdatabasePassword=tombolo_test
```

## Local deploy

To deploy to your local Maven installation (`~/.m2` by default):

```
gradle install
```

## Run Tasks

### Run export

We use the Gradle task `runExport` to run exports. The parameters are as follows:

```bash
gradle runExport \
    -PdataExportSpecFile='path/to/spec/file.json' \
    -PoutputFile='output_file.json' \
    -PforceImports='com.className'
    -PclearDatabaseCache=true
```

For example, this calculates the proportion of cycle traffic received at a traffic counter relative to the total traffic
in a given borough and outputs the results to the file `reaggregate-traffic-count-to-la.json`:

```bash
gradle runExport \
    -PdataExportSpecFile='src/main/resources/executions/examples/reaggregate-traffic-count-to-la.json' \
    -PoutputFile='reaggregate-traffic-count-to-la_output.json'
```

### Export data catalogue

We use the Gradle task `exportCatalogue` to export a JSON file detailing the capabilities of the connector
and explore the data catalogue.

```bash
gradle exportCatalogue -PoutputFile=catalogue.json
```

## Continuous Integration

We're using [Wercker](http://wercker.com/) for CI. Commits and PRs will be run
against the CI server automatically. If you don't have access, you can use the
Wercker account in the 1Password Servers vault to add yourself.

If you need to run the CI environment locally:

1. Install the [Wercker CLI](http://wercker.com/cli/install)
2. Run `wercker build`

The base image is generated with the very simple Dockerfile in the root of this
project. To push a new image to DockerHub you will need access to our DockerHub
account. If you don't have access, you can use the DockerHub account in the
1Password Servers vault to add yourself.

If you need new versions of PostgreSQL, Java, etc, you can update the image:

```
docker build -t tombolo .
docker images
# Look for `tombolo` and note the IMAGE ID
docker tag <IMAGE_ID> fcclab/tombolo:latest
docker push fcclab/tombolo
```

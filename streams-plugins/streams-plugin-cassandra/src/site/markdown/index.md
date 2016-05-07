org.apache.streams.plugins:streams-plugin-cassandra
===================================================

streams-plugin-cassandra generates resources from json schemas to assist with indexing of json data using Apache Cassandra.

#### Usage

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-cassandra:0.3-incubating-SNAPSHOT:cassandra

Output will be placed in target/generated-resources/cassandra by default

#### Example

[streams-plugin-cassandra/pom.xml](streams-plugin-cassandra/pom.xml "streams-plugin-cassandra/pom.xml")

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
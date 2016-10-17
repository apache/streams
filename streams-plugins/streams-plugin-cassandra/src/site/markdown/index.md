org.apache.streams.plugins:streams-plugin-cassandra
===================================================

streams-plugin-cassandra generates resources from json schemas to assist with indexing of json data using Apache Cassandra.

### Usage

##### Maven

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-cassandra:0.4-incubating:cassandra

[streams-plugin-cassandra/pom.xml](streams-plugin-cassandra/pom.xml "streams-plugin-cassandra/pom.xml")

##### SDK

Embed within your own java code

    StreamsCassandraGenerationConfig config = new StreamsCassandraGenerationConfig();
    config.setSourceDirectory("src/main/jsonschema");
    config.setTargetDirectory("target/generated-resources");
    StreamsCassandraResourceGenerator generator = new StreamsCassandraResourceGenerator(config);
    generator.run();
    
##### CLI
    
Run from CLI without Maven

    java -jar streams-plugin-cassandra-jar-with-dependencies.jar StreamsCassandraResourceGenerator src/main/jsonschema target/generated-resources

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
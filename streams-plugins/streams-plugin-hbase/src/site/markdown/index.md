org.apache.streams.plugins:streams-plugin-hbase
===============================================

streams-plugin-hbase generates resources from json schemas to assist with indexing of json data using Apache HBase.

### Usage

Output will be placed in target/generated-resources/hbase by default

[streams-plugin-hbase/pom.xml](streams-plugin-hbase/pom.xml "streams-plugin-hbase/pom.xml")

##### Maven

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-hbase:0.4-incubating:hbase

##### SDK

Embed within your own java code

    StreamsHbaseGenerationConfig config = new StreamsHbaseGenerationConfig();
    config.setSourceDirectory("src/main/jsonschema");
    config.setTargetDirectory("target/generated-resources");
    StreamsHbaseResourceGenerator generator = new StreamsHbaseResourceGenerator(config);
    generator.run();

##### CLI
 
Run from CLI without Maven

    java -jar streams-plugin-hbase-jar-with-dependencies.jar StreamsHbaseResourceGenerator src/main/jsonschema target/generated-resources

#### Example

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
org.apache.streams.plugins:streams-plugin-hive
==============================================

streams-plugin-hive generates resources from json schemas to assist with analysis of json data using Apache Hive.

### Usage

Output will be placed in target/generated-resources/hive by default

##### Maven

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-hive:0.4-incubating:generate-resources

[streams-plugin-hive/pom.xml](streams-plugin-hive/pom.xml "streams-plugin-hive/pom.xml")

##### SDK

Embed within your own java code

    StreamsHiveGenerationConfig config = new StreamsHiveGenerationConfig();
    config.setSourceDirectory("src/main/jsonschema");
    config.setTargetDirectory("target/generated-resources");
    StreamsHiveGenerationConfig generator = new StreamsHiveGenerationConfig(config);
    generator.run();
   
##### CLI
 
Run from CLI without Maven

    java -jar streams-plugin-hive-jar-with-dependencies.jar StreamsHiveResourceGenerator src/main/jsonschema target/generated-resources

#### Example

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
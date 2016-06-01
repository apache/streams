org.apache.streams.plugins:streams-plugin-pojo
==============================================

streams-plugin-pojo generates source files from json schemas suitable for writing Apache Streams components and libraries in Java.

### Usage

Output will be placed in target/generated-sources/pojo by default

##### Maven

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-pojo:0.3-incubating-SNAPSHOT:pojo

[streams-plugin-pojo/pom.xml](streams-plugin-pojo/pom.xml "streams-plugin-pojo/pom.xml")

##### SDK

Embed within your own java code

    StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();
    config.setSourceDirectory("src/main/jsonschema");
    config.setTargetDirectory("target/generated-resources");
    StreamsPojoSourceGenerator generator = new StreamsPojoSourceGenerator(config);
    generator.run();
  
##### CLI

Run from CLI without Maven

    java -jar streams-plugin-pojo-jar-with-dependencies.jar StreamsPojoSourceGenerator src/main/jsonschema target/generated-sources

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
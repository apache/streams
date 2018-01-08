org.apache.streams.plugins:streams-plugin-scala
=============================================

streams-plugin-scala generates source files from json schemas suitable for writing Apache Streams components and libraries in Scala.

### Usage

##### Maven

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-scala:0.5.1:generate-sources

[streams-plugin-scala/pom.xml](streams-plugin-scala/pom.xml "streams-plugin-scala/pom.xml")

##### SDK

Embed within your own java code

    StreamsScalaGenerationConfig config = new StreamsScalaGenerationConfig();
    config.setSourceDirectory("src/main/jsonschema");
    config.setTargetDirectory("target/generated-resources");
    StreamsScalaSourceGenerator generator = new StreamsScalaSourceGenerator(config);
    generator.run();

##### CLI

Run from CLI without Maven

    java -jar streams-plugin-scala-jar-with-dependencies.jar StreamsScalaSourceGenerator src/main/jsonschema target/generated-sources

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
org.apache.streams.plugins:streams-plugin-pig
=============================================

streams-plugin-pig generates resources from json schemas to assist with analysis of json data using Apache Pig.

### Usage

Output will be placed in target/generated-resources/pig by default

##### Maven

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-pig:0.4-incubating:pig

[streams-plugin-pig/pom.xml](streams-plugin-pig/pom.xml "streams-plugin-pig/pom.xml")

##### SDK

Embed within your own java code

    StreamsPigGenerationConfig config = new StreamsPigGenerationConfig();
    config.setSourceDirectory("src/main/jsonschema");
    config.setTargetDirectory("target/generated-resources");
    StreamsPigResourceGenerator generator = new StreamsPigResourceGenerator(config);
    generator.run();
  
##### CLI

Run from CLI without Maven

    java -jar streams-plugin-pig-jar-with-dependencies.jar StreamsPigResourceGenerator src/main/jsonschema target/generated-resources

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
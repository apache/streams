org.apache.streams.plugins:streams-plugin-elasticsearch
=======================================================

streams-plugin-elasticsearch generates resources from json schemas to assist with indexing of json data using Elasticsearch.

### Usage

Output will be placed in target/generated-resources/elasticsearch by default

##### Maven

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-elasticsearch:0.4-incubating:elasticsearch

[streams-plugin-elasticsearch/pom.xml](streams-plugin-elasticsearch/pom.xml "streams-plugin-elasticsearch/pom.xml")

##### SDK

Embed within your own java code

    StreamsElasticsearchGenerationConfig config = new StreamsElasticsearchGenerationConfig();
    config.setSourceDirectory("src/main/jsonschema");
    config.setTargetDirectory("target/generated-resources");
    StreamsElasticsearchResourceGenerator generator = new StreamsElasticsearchResourceGenerator(config);
    generator.run();
    
##### CLI
 
Run from CLI without Maven

    java -jar streams-plugin-elasticsearch-jar-with-dependencies.jar StreamsElasticsearchResourceGenerator src/main/jsonschema target/generated-resources

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
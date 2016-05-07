org.apache.streams.plugins:streams-plugin-elasticsearch
=======================================================

streams-plugin-elasticsearch generates resources from json schemas to assist with indexing of json data using Elasticsearch.

#### Usage

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-elasticsearch:0.3-incubating-SNAPSHOT:elasticsearch

Output will be placed in target/generated-resources/elasticsearch by default

#### Example

[streams-plugin-elasticsearch/pom.xml](streams-plugin-elasticsearch/pom.xml "streams-plugin-elasticsearch/pom.xml")

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
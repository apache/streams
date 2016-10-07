streams-persist-elasticsearch
=====================

Read/write to/from Elasticsearch

## Configuration

| Schema |
|--------|
| [ElasticsearchConfiguration.json](org/apache/streams/elasticsearch/ElasticsearchConfiguration.json "ElasticsearchConfiguration.json") [ElasticsearchConfiguration.html](apidocs/org/apache/streams/elasticsearch/ElasticsearchConfiguration.html "javadoc") |

## Components

![components](components.dot.svg "Components")

| Class | Configuration | Example Configuration(s) |
|-------|---------------|--------------------------|
| ElasticsearchPersistReader [ElasticsearchPersistReader.html](apidocs/org/apache/streams/elasticsearch/ElasticsearchPersistReader.html "javadoc") | [ElasticsearchReaderConfiguration.json](org/apache/streams/elasticsearch/ElasticsearchReaderConfiguration.json "ElasticsearchReaderConfiguration.json") [ElasticsearchReaderConfiguration.html](apidocs/org/apache/streams/elasticsearch/ElasticsearchReaderConfiguration.html "javadoc") | [elasticsearch-read.conf](elasticsearch-read.conf "elasticsearch-read.conf") |
| ElasticsearchPersistWriter [ElasticsearchPersistWriter.html](apidocs/org/apache/streams/elasticsearch/ElasticsearchPersistWriter "javadoc") | [ElasticsearchWriterConfiguration.json](org/apache/streams/elasticsearch/ElasticsearchWriterConfiguration.json "ElasticsearchWriterConfiguration.json") [ElasticsearchWriterConfiguration.html](apidocs/org/apache/streams/elasticsearch/ElasticsearchWriterConfiguration.html "javadoc") | [elasticsearch-write.conf](elasticsearch-write.conf "elasticsearch-write.conf") |
| ElasticsearchPersistUpdater [ElasticsearchPersistUpdater.html](apidocs/org/apache/streams/elasticsearch/ElasticsearchPersistUpdater "javadoc") | [ElasticsearchWriterConfiguration.json](org/apache/streams/elasticsearch/ElasticsearchWriterConfiguration.json "ElasticsearchWriterConfiguration.json") [ElasticsearchWriterConfiguration.html](apidocs/org/apache/streams/elasticsearch/ElasticsearchWriterConfiguration.html "javadoc") | [elasticsearch-write.conf](elasticsearch-write.conf "elasticsearch-write.conf") |

Testing:
---------

    mvn -PdockerITs docker:start
    mvn clean install test verify -DskipITs=false
    mvn -PdockerITs docker:stop
    
        
[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

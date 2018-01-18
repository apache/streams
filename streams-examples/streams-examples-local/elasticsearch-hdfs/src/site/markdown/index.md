### elasticsearch-hdfs

#### Requirements:
 - A running ElasticSearch 2.0.0+ instance

#### Streams:

<a href="HdfsElasticsearch.html" target="_self">HdfsElasticsearch</a>

<a href="ElasticsearchHdfs.html" target="_self">ElasticsearchHdfs</a>

#### Build:

    mvn clean install

#### Test:

Start up elasticsearch with docker:
     
    mvn -PdockerITs docker:start
 
Build with integration testing enabled:
 
    mvn clean test verify -DskipITs=false
 
Shutdown elasticsearch when finished:
 
    mvn -PdockerITs docker:stop

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
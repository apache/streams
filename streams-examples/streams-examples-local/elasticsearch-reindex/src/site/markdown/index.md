### elasticsearch-reindex

#### Requirements:
 - A running ElasticSearch 2.0.0+ cluster
 - Transport client access to cluster
 - elasticsearch.version and lucene.version set to match cluster

#### Streams:

<a href="ElasticsearchReindex.html" target="_self">ElasticsearchReindex</a>

#### Build:

    mvn clean install

#### Testing:

Start up elasticsearch with docker:
     
    mvn -PdockerITs docker:start
 
Build with integration testing enabled:
 
    mvn clean test verify -DskipITs=false
 
Shutdown elasticsearch when finished:
 
    mvn -PdockerITs docker:stop
    
[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
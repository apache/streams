### mongo-elasticsearch-sync

#### Requirements:
 - A running MongoDB 2.4+ instance
 - A running ElasticSearch 2.0+ instance

#### Streams:

<a href="MongoElasticsearchSync.html" target="_self">MongoElasticsearchSync</a>

#### Build:

    mvn clean package

#### Test:

Start up elasticsearch and mongodb with docker:
    
    mvn -PdockerITs docker:start

Build with integration testing enabled:

    mvn clean test verify -DskipITs=false

Shutdown elasticsearch and mongodb when finished:

    mvn -PdockerITs docker:stop

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

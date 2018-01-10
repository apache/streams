### twitter-userstream-elasticsearch

#### Requirements:
 - Authorized Twitter API credentials
 - A running ElasticSearch 1.0.0+ instance

#### Streams:

<a href="TwitterUserstreamElasticsearch.html" target="_self">TwitterUserstreamElasticsearch</a>

#### Build:

    mvn clean package

#### Test:

Start up elasticsearch with docker:
    
    mvn -PdockerITs docker:start

Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=twitter.oauth.conf"

Shutdown elasticsearch when finished:

    mvn -PdockerITs docker:stop

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

### twitter-history-elasticsearch

#### Requirements:
 - Authorized Twitter API credentials
 - A running ElasticSearch 2.0.0+ instance

#### Streams:

<a href="TwitterHistoryElasticsearch.html" target="_self">TwitterHistoryElasticsearch</a>

#### Build:

    mvn clean package
   
#### Test:

Create a local file `application.conf` with valid twitter credentials

    twitter {
      oauth {
        consumerKey = ""
        consumerSecret = ""
        accessToken = ""
        accessTokenSecret = ""
      }
    }
    
Start up elasticsearch with docker:
    
    mvn -PdockerITs docker:start

Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=twitter.oauth.conf"

Shutdown elasticsearch when finished:

    mvn -PdockerITs docker:stop

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

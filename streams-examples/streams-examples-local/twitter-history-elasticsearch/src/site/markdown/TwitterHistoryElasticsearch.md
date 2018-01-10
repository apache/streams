### TwitterHistoryElasticsearch

#### Description:

Retrieves as many posts from a known list of users as twitter API allows.

Converts them to activities, and writes them in activity format to Elasticsearch.

#### Configuration:

[TwitterHistoryElasticsearch.json](TwitterHistoryElasticsearch.json "TwitterHistoryElasticsearch.json") for _

##### application.conf

    include "elasticsearch.properties"
    include "elasticsearch.conf"
    include "twitter.oauth.conf"
    twitter {
      info = [
        18055613
      ]
      twitter.max_items = 1000
    }
    elasticsearch {
      index = twitter_history
      type = activity
      forceUseConfig = true
    }

[TwitterHistoryElasticsearchIT.conf](TwitterHistoryElasticsearchIT.conf "TwitterHistoryElasticsearchIT.conf")

#### Run (SBT):

    sbtx -210 -sbt-create
    set resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    set libraryDependencies += "org.apache.streams" % "twitter-history-elasticsearch" % "0.4-incubating-SNAPSHOT"
    set fork := true
    set javaOptions +="-Dconfig.file=application.conf"
    run org.apache.streams.example.TwitterHistoryElasticsearch

#### Run (Docker):

    docker run apachestreams/twitter-history-elasticsearch java -cp twitter-history-elasticsearch-jar-with-dependencies.jar -Dconfig.file=`pwd`/application.conf org.apache.streams.example.TwitterHistoryElasticsearch

#### Specification:

[TwitterHistoryElasticsearch.dot](TwitterHistoryElasticsearch.dot "TwitterHistoryElasticsearch.dot" )

#### Diagram:

![TwitterHistoryElasticsearch.dot.svg](./TwitterHistoryElasticsearch.dot.svg)

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

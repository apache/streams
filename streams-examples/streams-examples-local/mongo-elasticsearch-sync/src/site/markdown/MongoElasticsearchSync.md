### MongoElasticsearchSync

#### Description:

Copies documents from mongodb to elasticsearch

#### Configuration:

[MongoElasticsearchSync.json](MongoElasticsearchSync.json "MongoElasticsearchSync.json") for _

##### application.conf

    include "mongo.properties"
    include "mongo.conf"
    include "elasticsearch.properties"
    include "elasticsearch.conf"
    source = ${mongo}
    source {
      db: mongo_persist_it
      collection: activity
    }
    destination = ${elasticsearch}
    destination {
      index: mongo_elasticsearch_sync_it
      type: activity
      forceUseConfig": true
    }

#### Run (SBT):

    sbtx -210 -sbt-create
    set resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    set libraryDependencies += "org.apache.streams" % "mongo-elasticsearch-sync" % "0.6.1"
    set fork := true
    set javaOptions +="-Dconfig.file=application.conf"
    run org.apache.streams.example.MongoElasticsearchSync

#### Run (Docker):

    docker run apachestreams/mongo-elasticsearch-sync java -cp mongo-elasticsearch-sync-jar-with-dependencies.jar org.apache.streams.example.MongoElasticsearchSync

#### Specification:

[MongoElasticsearchSync.dot](MongoElasticsearchSync.dot "MongoElasticsearchSync.dot" )

#### Diagram:

![MongoElasticsearchSync.dot.svg](./MongoElasticsearchSync.dot.svg)

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
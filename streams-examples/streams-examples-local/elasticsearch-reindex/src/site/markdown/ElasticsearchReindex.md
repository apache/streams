### ElasticsearchReindex

#### Description:

Copies documents into a different index

#### Configuration:

[ElasticsearchReindex.json](ElasticsearchReindex.json "ElasticsearchReindex.json")

##### application.conf

    include "elasticsearch.properties"
    include "elasticsearch.conf"
    source = ${elasticsearch}
    source {
       indexes += "elasticsearch_persist_writer_it"
       types += "activity"
    }
    destination = ${elasticsearch}
    destination {
       index: "elasticsearch_reindex_it",
       type: "activity",
       forceUseConfig": true
    }
    
#### Run (SBT):

    sbtx -210 -sbt-create
    set resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    set libraryDependencies += "org.apache.streams" % "elasticsearch-reindex" % "0.6.1"
    set fork := true
    set javaOptions +="-Dconfig.file=application.conf"
    run org.apache.streams.example.ElasticsearchReindex

#### Run (Docker):

    docker run elasticsearch-reindex java -cp elasticsearch-reindex-jar-with-dependencies.jar -Dconfig.file=./application.conf org.apache.streams.example.ElasticsearchReindex

#### Specification:

[ElasticsearchReindex.dot](ElasticsearchReindex.dot "ElasticsearchReindex.dot" )

#### Diagram:

![ElasticsearchReindex.dot.svg](./ElasticsearchReindex.dot.svg)


###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
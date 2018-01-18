### FlinkTwitterFollowingPipeline

#### Description:

Collects twitter friends or followers with flink.

#### Configuration:

[TwitterFollowingPipelineConfiguration.json](TwitterFollowingPipelineConfiguration.json "TwitterFollowingPipelineConfiguration.json" )

    include "flink.conf"
    include "twitter.oauth.conf"
    source {
      fields = ["ID"]
      scheme = file
      path = "target/test-classes"
      readerPath = "asf.txt"
    }
    destination {
      fields = ["DOC"]
      scheme = file
      path = "target/test-classes"
      writerPath = "FlinkTwitterFollowingPipelineFriendsIT"
    }
    twitter {
      endpoint = friends
      ids_only = true
    }
    
#### Run (Local):

    java -cp dist/flink-twitter-collection-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline

#### Run (Flink):

    flink-run.sh dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline http://<location_of_config_file> 

#### Run (YARN):

    flink-run.sh yarn dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline http://<location_of_config_file> 

#### Specification:

[FlinkTwitterFollowingPipeline.dot](FlinkTwitterFollowingPipeline.dot "FlinkTwitterFollowingPipeline.dot" )

#### Diagram:

![FlinkTwitterFollowingPipeline.dot.svg](./FlinkTwitterFollowingPipeline.dot.svg)

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
### FlinkTwitterUserInformationPipeline

#### Description:

Collects twitter user profiles with flink.

#### Configuration:

[TwitterUserInformationPipelineConfiguration.json](TwitterUserInformationPipelineConfiguration.json "TwitterUserInformationPipelineConfiguration.json" )

    include "flink.conf"
    include "twitter.oauth.conf"
    source {
      fields = ["ID"]
      scheme = file
      path = "target/test-classes"
      readerPath = "1000twitterids.txt"
    }
    destination {
      fields = ["DOC"]
      scheme = file
      path = "target/test-classes"
      writerPath = "FlinkTwitterUserInformationPipelineIT"
    }
    
#### Run (Local):

    java -cp dist/flink-twitter-collection-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.examples.flink.twitter.collection.FlinkTwitterUserInformationPipeline

#### Run (Flink):

    flink-run.sh dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterUserInformationPipeline http://<location_of_config_file> 

#### Run (YARN):

    flink-run.sh yarn dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterUserInformationPipeline http://<location_of_config_file> 

#### Specification:

[FlinkTwitterUserInformationPipeline.dot](FlinkTwitterUserInformationPipeline.dot "FlinkTwitterUserInformationPipeline.dot" )

#### Diagram:

![TwitterUserInformationPipeline.dot.svg](./TwitterUserInformationPipeline.dot.svg)

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
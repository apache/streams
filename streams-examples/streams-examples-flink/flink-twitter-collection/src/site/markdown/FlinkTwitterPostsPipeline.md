### FlinkTwitterPostsPipeline

#### Description:

Collects twitter posts with flink.

#### Configuration:

[TwitterPostsPipelineConfiguration.json](TwitterPostsPipelineConfiguration.json "TwitterPostsPipelineConfiguration.json" )

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
      writerPath = "FlinkTwitterPostsPipelineIT"
    }
    
#### Run (Local):

    java -cp dist/flink-twitter-collection-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.examples.flink.twitter.collection.FlinkTwitterPostsPipeline

#### Run (Flink):

    flink-run.sh dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterPostsPipeline http://<location_of_config_file> 

#### Run (YARN):

    flink-run.sh yarn dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterPostsPipeline http://<location_of_config_file> 

#### Specification:

[FlinkTwitterPostsPipeline.dot](FlinkTwitterPostsPipeline.dot "FlinkTwitterPostsPipeline.dot" )

#### Diagram:

![FlinkTwitterPostsPipeline.dot.svg](./FlinkTwitterPostsPipeline.dot.svg)

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
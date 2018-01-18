### flink-twitter-collection

#### Requirements:
 - Authorized Twitter API credentials

#### Description:

Collects large batches of documents from api.twitter.com from a seed set of ids.

#### Streams:

<a href="FlinkTwitterFollowingPipeline.html" target="_self">FlinkTwitterFollowingPipeline</a>

<a href="FlinkTwitterPostsPipeline.html" target="_self">FlinkTwitterPostsPipeline</a>

<a href="FlinkTwitterSpritzerPipeline.html" target="_self">FlinkTwitterSpritzerPipeline</a>

<a href="FlinkTwitterUserInformationPipeline.html" target="_self">FlinkTwitterUserInformationPipeline</a>

#### Build:

    mvn clean package    

#### Test:

Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=twitter.oauth.conf"

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

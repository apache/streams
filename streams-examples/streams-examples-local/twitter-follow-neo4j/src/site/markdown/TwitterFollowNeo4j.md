### TwitterFollowNeo4j

#### Description:

Collects friend or follower connections for a set of twitter users to build a graph database in neo4j.

#### Configuration:

[TwitterFollowNeo4j.json](TwitterFollowNeo4j.json "TwitterFollowNeo4j.json") for _

##### application.conf

    include "neo4j.properties"
    include "neo4j.conf"
    include "twitter.oauth.conf"
    twitter {
      endpoint = "friends"
      info = [
        18055613
      ]
      twitter.max_items = 1000
    }

#### Run (SBT):

    sbtx -210 -sbt-create
    set resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    set libraryDependencies += "org.apache.streams" % "twitter-follow-neo4j" % "0.4-incubating-SNAPSHOT"
    set fork := true
    set javaOptions +="-Dconfig.file=application.conf"
    run org.apache.streams.example.TwitterFollowNeo4j

#### Run (Docker):

    docker run apachestreams/twitter-follow-neo4j java -cp twitter-follow-neo4j-jar-with-dependencies.jar org.apache.streams.example.TwitterFollowNeo4j

#### Specification:

[TwitterFollowNeo4j.dot](TwitterFollowNeo4j.dot "TwitterFollowNeo4j.dot" )

#### Diagram:

![TwitterFollowNeo4j.dot.svg](./TwitterFollowNeo4j.dot.svg)

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

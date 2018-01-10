### twitter-follow-neo4j

#### Requirements:
 - Authorized Twitter API credentials
 - A running Neo4J 3.0.0+ instance

#### Streams:

<a href="TwitterFollowNeo4j.html" target="_self">TwitterFollowNeo4j</a>

#### Build:

    mvn clean package    

#### Test:

Start up neo4j with docker:

    mvn -PdockerITs docker:start
    
Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=twitter.oauth.conf"

Shutdown neo4j when finished:

    mvn -PdockerITs docker:stop

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

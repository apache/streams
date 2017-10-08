## Neo4j

Start neo4j via docker with the docker maven plugin:

    mvn -PdockerITs docker:start

Confirm that neo4j is running:

    docker ps

Confirm that host and post(s) are in property file:

    cat neo4j.properties

Create a local file `neo4j.conf` with cluster details:

    neo4j {
      hostname = ${neo4j.tcp.host}
      port = ${neo4j.tcp.port}
      type = "neo4j"
      graph = "data"
    }

When configuring a stream, include these files:

    include "neo4j.properties"
    include "neo4j.conf"

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

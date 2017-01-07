## Mongo

Start mongo via docker with the docker maven plugin:

    docker -PdockerITs docker:start

Confirm that elasticsearch is running:

    docker ps

Confirm that host and post(s) are in property file:

    cat mongo.properties

Create a local file `elasticsearch.conf` with cluster details:

    mongo {
      host = ${mongo.tcp.host}
      port = ${mongo.tcp.port}
    }

When configuring a stream, include these files:

    include "mongo.properties"
    include "mongo.conf"

Supply application-specific configuration as well:

    mongo {
        db: "",
        collection: ""
    }

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

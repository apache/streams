## Riak

Start riak via docker with the docker maven plugin:

    mvn -PdockerITs docker:start

Confirm that riak is running:

    docker ps

Confirm that host and post(s) are in property file:

    cat riak.properties

Create a local file `riak.conf` with cluster details:

    riak {
      scheme = "tcp"
      hosts += ${riak.tcp.host}
      port = ${riak.tcp.port}
      defaultBucket = "binary"
    }

When configuring a stream, include these files:

    include "riak.properties"
    include "riak.conf"

When running integration testing, riak.properties must be in the root of the streams project repository.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

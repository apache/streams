## Cassandra

Start cassandra via docker with the docker maven plugin:

    docker -PdockerITs docker:start

Confirm that cassandra is running:

    docker ps

Confirm that host and post(s) are in property file:

    cat cassandra.properties

Create a local file `cassandra.conf` with cluster details:

    cassandra {
      host = ${cassandra.tcp.host}
      port = ${cassandra.tcp.port}
    }

When configuring a stream, include these files:

    include "cassandra.properties"
    include "cassandra.conf"

Supply application-specific configuration as well:

    cassandra {
      keyspace = test_keyspace1
      table = test_table1
      partitionKeyColumn = key
      column = value
    }

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

## Elasticsearch

Start elasticsearch via docker with the docker maven plugin:

    mvn -PdockerITs docker:start

Confirm that elasticsearch is running:

    docker ps

Confirm that host and post(s) are in property file:

    cat elasticsearch.properties

Create a local file `elasticsearch.conf` with cluster details:

    elasticsearch {
      hosts += ${es.tcp.host}
      port = ${es.tcp.port}
      clusterName = "elasticsearch"
    }

When configuring a stream, include these files:

    include "elasticsearch.properties"
    include "elasticsearch.conf"

Supply application-specific configuration as well:

    elasticsearch {
        index: ""
        type: ""
    }

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

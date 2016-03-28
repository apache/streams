streams-persist-elasticsearch
=====================

Read and write to Elasticsearch

Example reader configuration:

    "elasticsearch": {
        "hosts": [
            "localhost"
        ],
        "port": 9300,
        "clusterName": "elasticsearch",
        "indexes": [
            "sourceindex"
        ],
        "types": [
            "sourcetype"
        ],
        "_search": {
            "query" : {
                "match_all" : { }
            }
        }
    }

Example writer configuration:

    "elasticsearch": {
        "hosts": [
            "localhost"
        ],
        "port": 9300,
        "clusterName": "elasticsearch",
        "index": "destinationindex",
        "type": "destinationtype
    }

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

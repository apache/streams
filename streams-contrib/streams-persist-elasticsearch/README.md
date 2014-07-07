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


streams-persist-graph
=====================

Build graph index of stream

Example Neo4J configuration:

    {
        "graph": {
            "type": "neo4j",
            "protocol": "http",
            "hostname": "localhost",
            "port": 7474,
            "graph": "data"
            "vertices": {
                "verbs": [
                    "post",
                    "share",
                    "tweet"
                ],
                "objectType": "page"
            }
        },
    }

Example Rexster configuration:

    {
        "graph": {
            "type": "rexster",
            "protocol": "http",
            "hostname": "localhost",
            "port": 8182,
            "graph": "data",
            "vertices": {
                "verbs": [
                    "post",
                    "share",
                    "tweet"
                ],
                "objectType": "page"
            }
        },
    }
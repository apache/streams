Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

streams-persist-graph
=====================

Build a graph index of a stream

Example Neo4J writer configuration:

    {
        "graph": {
            "type": "neo4j",
            "protocol": "http",
            "hostname": "localhost",
            "port": 7474,
            "graph": "data"
            "vertices": {
                "objects": [
                    "actor",
                    "object"
                ],
                "verbs": [
                    "follow"
                ],
                "objectTypes": [
                    "page"
                ]
            }
        },
    }

Example Neo4J reader configuration:

    {
        "graph": {
            "type": "neo4j",
            "protocol": "http",
            "hostname": "localhost",
            "port": 7474,
            "graph": "data"
            "query": {
                "objects": [
                    "actor",
                    "object"
                ],
                "verbs": [
                    "follow"
                ],
                "objectTypes": [
                    "page"
                ]
            }
        },
    }


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

For more examples, see:

- [elasticsearch-backup](http://github.com/w2ogroup/streams-examples/tree/master/elasticsearch-backup)
- [elasticsearch-reindex](http://github.com/w2ogroup/streams-examples/tree/master/elasticsearch-reindex)
- [elasticsearch-restore](http://github.com/w2ogroup/streams-examples/tree/master/elasticsearch-restore)
- [mongo-elasticsearch-index](https://github.com/w2ogroup/streams-examples/tree/master/mongo-elasticsearch-index)
- [twitter-gardenhose-elasticsearch](https://github.com/w2ogroup/streams-examples/tree/master/twitter-gardenhose-elasticsearch)
- [twitter-history-elasticsearch](https://github.com/w2ogroup/streams-examples/tree/master/twitter-history-elasticsearch)
- [twitter-userstream-elasticsearch](https://github.com/w2ogroup/streams-examples/tree/master/twitter-userstream-elasticsearch)
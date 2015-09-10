streams-persist-kinesis
==============

Read/Write documents to/from Kinesis.

Example writer configuration:

    "kinesis": {
        "key": "",
        "secretKey": "",
        "protocol": "HTTPS",
        "region": "us-east-1",
        "streams: [
            "topic1",
            "topic2"
        ]
    }


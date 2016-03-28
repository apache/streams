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

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

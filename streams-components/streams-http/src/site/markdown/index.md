streams-processor-http
=====================

Hit an http endpoint and place the result in extensions

Example SimpleHTTPGetProcessor configuration:

    "http": {
        "protocol": "http",
        "hostname": "urls.api.twitter.com",
        "port": 9300,
        "resourceUri": "1/urls/count.json"
    }

Example SimpleHTTPPostPersistWriter configuration:

    "http": {
        "protocol": "http",
        "hostname": "localhost",
        "port": 7474,
        "resourceUri": "db/data/cypher"
    }

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

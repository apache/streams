org.apache.streams.plugins:streams-plugin-hbase
===============================================

streams-plugin-hbase generates resources from json schemas to assist with indexing of json data using Apache HBase.

#### Usage

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-hbase:0.3-incubating-SNAPSHOT:hbase

Output will be placed in target/generated-resources/hive by default

#### Example

[streams-plugin-hbase/pom.xml](streams-plugin-hbase/pom.xml "streams-plugin-hbase/pom.xml")

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
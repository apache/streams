org.apache.streams.plugins:streams-plugin-pojo
==============================================

streams-plugin-pojo generates source files from json schemas suitable for writing Apache Streams components and libraries in Java.

#### Usage

Run within a module containing a src/main/jsonschema directory

    mvn org.apache.streams.plugins:streams-plugin-pojo:0.3-incubating-SNAPSHOT:pojo

Output will be placed in target/generated-sources/pojo by default

#### Example

[streams-plugin-pojo/pom.xml](streams-plugin-pojo/pom.xml "streams-plugin-pojo/pom.xml")

#### Documentation

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
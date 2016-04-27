streams-persist-graph
=====================

Build a graph index of a stream

## Configuration

| Schema |
|--------|
| [GraphConfiguration.json](org/apache/streams/graph/GraphConfiguration.json "GraphConfiguration.json") [GraphConfiguration.html](apidocs/org/apache/streams/graph/GraphConfiguration.html "javadoc") |

## Components

![components](components.dot.svg "Components")

| Class | Configuration | Example Configuration(s) |
|-------|---------------|--------------------------|
| GraphVertexReader [GraphVertexReader.html](apidocs/org/apache/streams/graph/GraphVertexReader.html "javadoc") | [GraphReaderConfiguration.json](org/apache/streams/graph/GraphReaderConfiguration.json "GraphReaderConfiguration.json") [GraphReaderConfiguration.html](apidocs/org/apache/streams/graph/GraphReaderConfiguration.html "javadoc") | [graph-read.conf](graph-read.conf "graph-read.conf") |
| GraphHttpPersistWriter [GraphHttpPersistWriter.html](apidocs/org/apache/streams/graph/GraphHttpPersistWriter "javadoc") | [GraphHttpConfiguration.json](org/apache/streams/graph/GraphHttpConfiguration.json "GraphHttpConfiguration.json") [GraphHttpConfiguration.html](apidocs/org/apache/streams/graph/GraphHttpConfiguration.html "javadoc") | [graph-write.conf](graph-write.conf "graph-write.conf") |

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

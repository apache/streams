streams-persist-kinesis
==============

Read/Write documents to/from Kinesis.

## Configuration

| Schema |
|--------|
| [KinesisConfiguration.json](org/apache/streams/amazon/kinesis/KinesisConfiguration.json "KinesisConfiguration.json") [KinesisConfiguration.html](apidocs/org/apache/streams/amazon/kinesis/KinesisConfiguration.html "javadoc") |

## Components

![components](components.dot.svg "Components")

| Class | Configuration | Example Configuration(s) |
|-------|---------------|--------------------------|
| KinesisPersistReader [KinesisPersistReader.html](apidocs/org/apache/streams/amazon/kinesis/KinesisPersistReader.html "javadoc") | [KinesisReaderConfiguration.json](org/apache/streams/amazon/kinesis/KinesisReaderConfiguration.json "KinesisReaderConfiguration.json") [KinesisReaderConfiguration.html](apidocs/org/apache/streams/amazon/kinesis/KinesisReaderConfiguration.html "javadoc") | [kinesis-read.conf](kinesis-read.conf "kinesis-read.conf") |
| KinesisPersistWriter [KinesisPersistWriter.html](apidocs/org/apache/streams/amazon/kinesis/KinesisPersistWriter "javadoc") | [KinesisWriterConfiguration.json](org/apache/streams/amazon/kinesis/KinesisWriterConfiguration.json "KinesisWriterConfiguration.json") [KinesisWriterConfiguration.html](apidocs/org/apache/streams/amazon/kinesis/KinesisWriterConfiguration.html "javadoc") | [kinesis-write.conf](kinesis-write.conf "kinesis-write.conf") |

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

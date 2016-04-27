streams-persist-s3
==============

Write documents to an S3 bucket.

## Configuration

| Schema |
|--------|
| [S3Configuration.json](org/apache/streams/s3/S3Configuration.json "S3Configuration.json") [S3Configuration.html](apidocs/org/apache/streams/s3/S3Configuration.html "javadoc") |

## Components

![components](components.dot.svg "Components")

| Class | Configuration | Example Configuration(s) |
|-------|---------------|--------------------------|
| S3PersistReader [S3PersistReader.html](apidocs/org/apache/streams/s3/S3PersistReader.html "javadoc") | [KinesisReaderConfiguration.json](org/apache/streams/s3/KinesisReaderConfiguration.json "KinesisReaderConfiguration.json") [KinesisReaderConfiguration.html](apidocs/org/apache/streams/s3/KinesisReaderConfiguration.html "javadoc") | [s3-read.conf](s3-read.conf "s3-read.conf") |
| S3PersistWriter [S3PersistWriter.html](apidocs/org/apache/streams/s3/S3PersistWriter "javadoc") | [KinesisWriterConfiguration.json](org/apache/streams/s3/KinesisWriterConfiguration.json "KinesisWriterConfiguration.json") [KinesisWriterConfiguration.html](apidocs/org/apache/streams/s3/KinesisWriterConfiguration.html "javadoc") | [s3-write.conf](s3-write.conf "s3-write.conf") |

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

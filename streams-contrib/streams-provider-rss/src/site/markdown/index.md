streams-provider-rss
====================

## Data Types

| Schema |
|--------|
| [contents.xsd](http://www.dlese.org/Metadata/opml/2.0/contents.xsd "contents.xsd") [FeedDetails.html](apidocs/org/apache/streams/rss/FeedDetails.html "javadoc") |

## Components

![components](components.dot.svg "Components")

| Class | Configuration | Example Configuration(s) |
|-------|---------------|--------------------------|
| RssStreamProvider [RssStreamProvider.html](apidocs/org/apache/streams/rss/provider/RssStreamProvider.html "javadoc") | [RssStreamConfiguration.json](RssStreamConfiguration.json "RssStreamConfiguration.json") [RssStreamConfiguration.html](apidocs/org/apache/streams/rss/RssStreamConfiguration.html "javadoc") | [rss.conf](rss.conf "rss.conf") |

Test:
-----

Build with integration testing enabled

    mvn clean test verify -DskipITs=false
    
[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

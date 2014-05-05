streams-processor-urls

Purpose

  Module makes http calls to links and preserves content and metadata for downstream processing.

Prerequisites

  * String value array in activity.links

Optionally, module can convert messages to ActivityStreams format

  * LinkResolverProcessor [LinkResolverProcessor]
  * LinkCrawlerProcessor [LinkCrawlerProcessor]

[LinkResolverProcessor]: LinkResolverProcessor

  LinkResolverProcessor.class processes activities like this:

  ![LinkResolverProcessor.png](LinkResolverProcessor.png)

[LinkCrawlerProcessor]: LinkCrawlerProcessor

  LinkCrawlerProcessor.class processes activities like this:

  ![LinkCrawlerProcessor.png](LinkCrawlerProcessor.png)
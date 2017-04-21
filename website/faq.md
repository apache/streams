---
layout: default
title:
description:
group:

---

## Frequently Asked Questions

###    Why should I adopt activity streams for my project?

Odds are the dataset you are working with is some combination of timestamped events and observations of entities and their relationships at various points in time.  Activity Streams provides a simple yet powerful standard format for these types of data, regardless of their origin, publisher, or specific details.  Activity Streams is a community-driven specification designed for interoperability and flexibility.  By supporting activity streams you maximize the chance that a new data-source of interest to you will be compatible with your existing data, and that your data will be compatible with that of other communities working on similar projects.  

###    What organizations exchange data in activity streams formats?

A short list of organizations and products that support activity streams format is compiled [here](who.html "here").

If your organization supports activity streams, please let us know on the project mailing list.

###    Why should I consider using Apache Streams for my project?

If you are working with structured event and or entity data that fits the Activity Streams model, and working with a JVM language, Apache Streams can simplify many of the challenging aspects involved with these types of projects.  For example:

* Keeping track of the original source of each piece of information
* Harmonizing a multitude of date-time formats
* Moving between JSON, XML, YAML, and binary serializations
* Writing processing logic that can run in both batch and real-time workflows
* Defining constraints and validation rules for up-stream (third-party) and in-stream (your sphere of control) data
* Supplying run-time configuration globally and per-stream-component in a sensible manner

###    What does Apache Streams actually do?

Apache Streams is

* an SDK for data-centric JVM software
* a set of modules that connect data-providing APIs and data-persisting analytical systems
* a community working to make web and enterprise datasets interoperable by default

Apache Streams is not

* one-size-fits-all
* prescriptive or opinionated about how it should be used
* only useful for projects fully dedicated to activity streams datasets

The primary Streams git repository incubator-streams (org.apache.streams:streams-project) contains

* core interfaces and utilities
* plugins for transforming schemas into source code and other artifacts
* a library of modules for acquiring, transforming, and enriching data streams.

Similar modules can also be hosted externally - so long as they publish maven artifacts compatible with your version of streams, you can import and use them in your streams easily.

The streams community also supports a separate repository incubator-streams-examples (org.apache.streams:streams-examples) which contains a library of simple streams that are 'ready-to-run'.  Look here to see what Streams user code look like.

###    Why bother with any data framework at all?

Why use Postgres, Elasticsearch, Cassandra, Hadoop, Linux, or Java?

Frameworks make important but boring parts of systems and code just work so your team can focus on features important to your users.

If you are sure you can write code that is some combination of faster, more readable, better tested, easier to learn, easier to build with, or more maintainable than any existing framework (including Streams), maybe you should.

But you are probably under-estimating how difficult it will be to optimize across all of these considerations, stay current with upgrades to underlying libraries, and fix whatever bugs are discovered.

Or maybe you are capable of doing it all flawlessly, but your time is just more valuable focused on your product rather than on plumbing.

By joining forces with others who care about clean running water, everyone can run better, faster, stronger code assembled with more diverse expertise, tested and tuned under more use cases.

###    How is streams different than "*processing framework*"?

You don't have to look hard to find great data processing frameworks for batch or for real-time.  Pig, Hive, Storm, Spark, Samza, Flink, and Google Cloud Dataflow (soon-to-be Apache Beam) are all great.  Apex and NiFi are interesting newer options.  This list only includes Apache Foundation JVM projects!

At the core these platforms help you connect inputs and  outputs to a directed graph of computation, and run your code at scale.

Streams use this computational model as well, but is more focused on intelligently and correctly modeling the data that will flow through the stream than on stream execution.  In this sense Streams is an alternative to avro or protocol buffers - one which prioritizes flexibility, expressivity, interoperability, and tooling ahead of speed or compute efficiency.

Streams seeks to make it easy to design and evolve streams, and to configure complex streams sensibly.  Where many processing frameworks leave all business logic and configuration issues to the developer, streams modules are designed to mix-and-match.  Streams modules expect to be embedded with other frameworks and are organized to make that process painless.

Streams also contains a library of plug-and-play data providers to collect and normalize data from a variety of popular sources.

###    How do I deploy Streams?

Currently you cannot deploy Streams (uppercase).  Streams has no shrink-wrapped ready-to-run server process.  You can however deploy streams (lowercase).  The right method for packaging, deploying, and running streams depends on what runtime you are going to use.

Streams includes a local runtime that uses multi-threaded execution and blocking queues within a single process.  In this scenario you build an uberjar with few exclusions and ship it to a target environment however you want - maven, scp, docker, etc...  You launch the stream process with an appropriate configuration and watch the magic / catastrophic fail.

Alternatively, components written to streams interfaces can be bound within other platforms such as pig or spark.  In this scenario, you build an uberjar that excludes the platform parts of the classpath and launch your stream using the launch style of that platform.

###    Can't I just dump source data directly into files or databases?

Absolutely - and that will work great right up until the point where the requirements, the tools, or the way you want to index your data need to change.

###    What if I need data from "*specific API*"?

No problem - anyone can write a Streams provider.  The project contains providers that use a variety of strategies to generate near-real-time data streams, including:

* sockets
* webhooks
* polling
* scraping

Providers can run continuously and pass-through new data, or they can work sequentially through a backlog of items.  If you need to collect so many items that you can't fit all of their ids in the memory available to your stream, it's pretty simple to sub-divide your backlog into small batches and launch a series of providers for collection using frameworks such as Flink or Spark Streaming.

###    What if I want to keep data in "*unsupported database*"?

No problem - anyone can write a Streams persist reader or persist writer.  The project contains persist writers that:

* write documents efficiently with batch-style binary indexing
* write documents one-by-one to services with REST api endpoints
* write data to local or distributed buffers.

If you just want to use streams providers to collect and feed incoming data into a queueing system to work with outside of streams that's just fine.

###    Can't I just use "*third-party SDK*" to do the same thing?

Describe any specific data collection, processing, or storage function and there are probably several if not tens of basic implementations on GitHub.  There may even be language-specific libraries published by a vendor with a commercial interest in a related technology.

However, in general there are a set of tradeoffs involved when relying on these packages.

* They often have transitive dependencies.
* They may not use performant HTTP and JSON libraries.
* The object representations and lifecycle mechanisms they provide may not be consistent with the rest of your code.
* They may source configuration properties in a problematic or cumbersome fashion.
* Their licenses may be restrictive or undocumented.

Streams goes to great lengths to regularize many of these issues so that they are uniform across project modules, and easy to reuse within new and external modules.

Where quality java libraries exist, their most useful parts may be included within a streams module, with unnecessary or difficult parts of their dependency tree excluded.

###    Where do I start?

Work your way through the 'Tutorial' menu to get up and running with streams.

Then browse the 'Other Resources' menu to learn more about how streams works and why.

###    How can I help?

* Join our mailing list.
* Ask questions and suggest features.
* Contribute to the documentation in one of the streams repositories.
* Write a new provider using an existing provider as a template.
* Add new features (and / or tests) to an existing module you intend to use.
* Build and contributing a new example.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

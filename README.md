Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

About
=====
Apache Streams is a lightweight (yet scalable) server for ActivityStreams. The role of Apache Streams is to provide a central point of aggregation, filtering and querying for Activities that have been submitted by disparate systems. Apache Streams also intends to include a mechanism for intelligent filtering and recommendation to reduce the noise to end users.

Release Notes
=============

[CHANGELOG.md](CHANGELOG.md "CHANGELOG.md")

Getting Started
===============

Please visit the project website for the latest information:
    http://streams.incubator.apache.org/

Check out the following repository for ready-to-run examples:
    https://git-wip-us.apache.org/repos/asf/incubator-streams-examples.git

Along with the developer mailing list archive:
    http://mail-archives.apache.org/mod_mbox/streams-dev/

Auto-generated documentation and resources are located here:
    http://streams.incubator.apache.org/site/0.2-incubating/streams-project/

System Requirements
===================
You need a platform that supports Java SE 7 or later.

  - Sources compilation require Java SE 7 or higher.

  - Streams currently requires Java 1.7.0u72+, or Java 1.8.0u25+ during build phase.
    
  - The project is built with Apache Maven 3+ (suggested is 3.2.5).

Building
====================
To build from source code:
    
  - Invoke maven in the root directory.

      `export MAVEN_OPTS="-Xmx2G"`

      `mvn install`


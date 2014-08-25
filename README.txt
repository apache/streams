Apache Streams (incubating) - README.txt
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

About
=====
Apache Streams is a lightweight (yet scalable) server for ActivityStreams. The role of Apache Streams is to provide a central point of aggregation, filtering and querying for Activities that have been submitted by disparate systems. Apache Streams also intends to include a mechanism for intelligent filtering and recommendation to reduce the noise to end users.



Release Notes
=============
 - Initial project structure and master POM release


Getting Started
===============
Please visit the project website for the latest information:
    http://streams.incubator.apache.org/

Along with the developer mailing list archive:
    http://mail-archives.apache.org/mod_mbox/streams-dev/


System Requirements
===================
You need a platform that supports Java SE 6 or later.

Building and running
====================
To build from source code:

  - Requirements:
    Sources compilation require Java SE 6 or higher.
    The project is built with Apache Maven 3+ (suggested is 3.0.3).
    You need to download and install Maven 3 from: http://maven.apache.org/

  - The Streams project itself (this one) depends on the separate Streams Master project
    which defines general and global settings for the whole of the Streams project,
    independent of a specific release.
    As its streams-master-pom is already published to the Apache Snapshots repository,
    there is no need to check it out manually and build it locally yourself,
    unless changes are needed on general and global level.
    
    If so needed, the Streams Master project can be checked out from:
      http://svn.apache.org/repos/asf/incubator/streams/streams-master-pom/trunk streams-master-pom

    After check out, cd into streams-master-pom and invoke maven to install it using:
      $mvn install
    
  - To build the Streams project invoke maven in the root directory:
      $mvn install

## Overview
Apache Streams unifies a diverse world of digital profiles and online activities into common formats and vocabularies, and makes these datasets accessible across a variety of databases, devices, and platforms for streaming, browsing, search, sharing, and analytics use-cases.

### How do I use Streams?
This repository contains officially maintained examples of how to compose and run streams in various contexts.

### Getting Started

Please visit the project website for the latest information:
    http://streams.apache.org/

Along with the developer mailing list archive:
    https://lists.apache.org/list.html?dev@streams.apache.org

### Building and running
To build from source code:

  - Requirements:
    Sources compilation require Java SE 8 or higher.
    Sources compilation require Apache Maven 3.3.9 or higher.
    You need to download and install Maven 3 from: http://maven.apache.org/

  - The Streams Examples project itself (this one) depends on the separate Streams project
    which contains the source code and poms for Apache Streams.
    As streams-project is already published to the Apache Releases repository,
    there is no need to check it out manually and build it locally yourself,
    unless you choose to checkout a SNAPSHOT branch.
    
    - If so needed, streams can be checked out from:
      http://git-wip-us.apache.org/repos/asf/streams.git

    - After check out, cd into streams and invoke maven to install it using:
      
            mvn install
   
  - To build all of the Streams examples, invoke maven with:
      
        mvn install

### Integration Testing

  *Integration Testing will fail unless you have a working docker installation and valid twitter credentials in local conf files.*
  
  - To run the full suite of integration tests, invoke maven with:
        
        mvn -N -PdockerITs docker:start
        mvn clean verify -D
        mvn -N -PdockerITs docker:stop

  - To build and install a docker image containing a specific example, change to that example's directory then:
      
        mvn -Pdocker clean package docker:build

### Disclaimer
Apache Streams is an effort undergoing incubation at [The Apache Software Foundation (ASF)](http://apache.org) sponsored by the [Apache Incubator PMC](http://incubator.apache.org). Incubation is required of all newly accepted projects until a further review indicates that the infrastructure, communications, and decision making process have stabilized in a manner consistent with other successful ASF projects. While incubation status is not necessarily a reflection of the completeness or stability of the code, it does indicate that the project has yet to be fully endorsed by the ASF.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

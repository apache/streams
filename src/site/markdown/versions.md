## Versions Supported

Apache Streams intends to be buildable and usable by a wide variety of users.

To that end, we seek for our code to be compatible with a wide variety of Java versions.

Typically, maven will determine the version of Java to build with based on the active JDK.

It does so by enabling one of the following profiles:
  
  - jdk8
  - jdk11
  - jdk17

The artifacts published to maven central with no classifier are built with JDK17.

The artifacts published to maven central with the classifier `jdk11` are built with JDK11.

The artifacts published to maven central with the classifier `jdk8` are built with JDK8.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
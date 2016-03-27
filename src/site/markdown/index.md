Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

# org.apache.streams:streams-project

Release Notes
=============

[CHANGELOG.md](CHANGELOG.md "CHANGELOG.md")

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

Additional Documentation
========================

[Modules](modules.md "Modules")

[JavaDocs](apidocs/index.html "JavaDocs")

[TestJavaDocs](testapidocs/index.html "TestJavaDocs")

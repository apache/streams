streams-provider-instagram
==========================

Module connects to instagram API, collects events, converts to activity, and passes each activity downstream.

## Data Types

This module relies on classes from com.sachinhandiekar:jInstagram

## Configuration

| Schema |
|--------|
| [InstagramConfiguration.json](com/instagram/InstagramConfiguration.json "InstagramConfiguration.json") [InstagramConfiguration.html](apidocs/com/instagram/InstagramConfiguration.html "javadoc") |
| [InstagramUserInformationConfiguration.json](com/instagram/InstagramUserInformationConfiguration.json "InstagramUserInformationConfiguration.json") [InstagramUserInformationConfiguration.html](apidocs/com/instagram/InstagramUserInformationConfiguration.html "javadoc") |

## Components

![components](components.dot.svg "Components")

Test:
-----

Create a local file `instagram.conf` with valid instagram credentials

    instagram {
      clientId = "b389fcbeca2a40a89afa591a8468e4dc"
      usersInfo = {
        authorizedTokens = [
          "1646021441.b739937.58d5b84abce74241b640d8c1f7e91222"
        ]
      }
    }
    
Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=`pwd`/instagram.conf"

If you only have sandbox credentials, you'll have to edit src/test/resources/*IT.conf to pull ids that have been authorized on your sandbox. 

Resources:
----------

[https://elfsight.com/blog/2016/05/how-to-get-instagram-access-token/](https://elfsight.com/blog/2016/05/how-to-get-instagram-access-token/ "https://elfsight.com/blog/2016/05/how-to-get-instagram-access-token/")

[https://smashballoon.com/instagram-feed/find-instagram-user-id/](https://smashballoon.com/instagram-feed/find-instagram-user-id/ "https://smashballoon.com/instagram-feed/find-instagram-user-id/")

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

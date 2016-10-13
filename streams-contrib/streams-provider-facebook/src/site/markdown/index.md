streams-provider-facebook
==============

streams-provider-facebook contains schema definitions, providers, conversions, and utility classes.

## Data Types

[Page.json](org/apache/streams/facebook/Page.json "Page.json")

[Post.json](org/apache/streams/facebook/graph/Post.json "Post.json")

## Configuration

| Schema |
|--------|
| [FacebookConfiguration.json](org/apache/streams/facebook/FacebookConfiguration.json "FacebookConfiguration.json") [FacebookConfiguration.html](apidocs/org/apache/streams/facebook/FacebookConfiguration.html "javadoc") |

## Components

![components](components.dot.svg "Components")

Test:
-----

Create a local file `facebook.conf` with valid twitter credentials

    facebook {
      oauth {
        appId = ""
        appSecret = ""
      }
      userAccessTokens = [
        ""
      ]
    }
    
Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=`pwd`/facebook.conf"

Confirm that you can get data from testing endpoints with Graph API Explorer:
 
 - ${page-id}
 - ${page-id}/feed

[https://developers.facebook.com/tools/explorer/](https://developers.facebook.com/tools/explorer/ "https://developers.facebook.com/tools/explorer/")
  
  
[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

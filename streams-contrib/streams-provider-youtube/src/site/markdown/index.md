org.apache.streams:streams-provider-youtube
===========================================

streams-provider-youtube contains providers, conversions, and utility classes.

## Data Types

This module relies on classes from com.google.apis:google-api-services-youtube

## Configuration

| Schema |
|--------|
| [YoutubeConfiguration.json](com/youtube/YoutubeConfiguration.json "YoutubeConfiguration.json") [YoutubeConfiguration.html](apidocs/com/youtube/Youtube.html "javadoc") |

## Components

![components](components.dot.svg "Components")

| Class | Configuration |
|-------|---------------|
| YoutubeChannelProvider [YoutubeChannelProvider.html](apidocs/com/youtube/provider/YoutubeChannelProvider.html "javadoc") | [YoutubeConfiguration.json](com/youtube/YoutubeConfiguration.json "YoutubeConfiguration.json") [YoutubeConfiguration.html](apidocs/com/youtube/YoutubeConfiguration.html "javadoc")
| YoutubeUserActivityProvider [YoutubeUserActivityProvider.html](apidocs/com/youtube/provider/YoutubeUserActivityProvider.html "javadoc") | [YoutubeConfiguration.json](com/youtube/YoutubeConfiguration.json "YoutubeConfiguration.json") [YoutubeConfiguration.html](apidocs/com/youtube/YoutubeConfiguration.html "javadoc")

Test:
-----

Log into admin console
Create project
Enable Data API on project
Create service account
Download p12 file

Create a local file `youtube.conf` with valid youtube credentials

    youtube {
      apiKey = ""
      oauth {
        serviceAccountEmailAddress = ""
        pathToP12KeyFile = ""
      }
    }
    
Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=`pwd`/youtube.conf"

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

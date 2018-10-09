org.apache.streams:google-gplus
===============================

google-gplus contains providers, conversions, and utility classes for activity exchange with Google+

## Configuration

| Schema | JavaDoc | Example Configuration(s) |
|--------|---------|--------------------------|
| [GPlusConfiguration.json](com/google/gplus/GPlusConfiguration.json "GPlusConfiguration.json") | [GPlusConfiguration.html](apidocs/com/google/gplus/GPlusConfiguration.html "GPlusConfiguration.html") | [reference.conf](reference.conf "reference.conf") |

## Components

![components](components.dot.svg "Components")

| Class | Class JavaDoc | 
|-------|---------------|
| GPlusUserDataProvider | [GPlusUserDataProvider.html](apidocs/com/google/gplus/provider/GPlusUserDataProvider.html "GPlusUserDataProvider.html") |
| GPlusUserActivityProvider | [GPlusUserActivityProvider.html](apidocs/com/google/gplus/provider/GPlusUserActivityProvider.html "GPlusUserActivityProvider.html") |
| GooglePlusTypeConverter | [GooglePlusTypeConverter.html](apidocs/com/google/gplus/processor/GooglePlusTypeConverter.html "GooglePlusTypeConverter.html") |

Test:
-----

Log into admin console
Create project
Enable Data API on project
Create service account
Download p12 file

Create a local file `gplus.conf` with valid gplus credentials

    gplus {
      apiKey = ""
      oauth {
        serviceAccountEmailAddress = ""
        pathToP12KeyFile = ""
      }
    }
    
Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=`pwd`/gplus.conf"

[Experimental Features](experimental.html "Experimental Features")
    
[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

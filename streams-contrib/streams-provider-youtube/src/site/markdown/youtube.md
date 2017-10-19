## YouTube Credentials

Log into developers.google.com

Visit https://console.developers.google.com/apis/credentials

The list of projects is in a top-left dropdown - select or create one.

Create a local file `youtube.conf` with valid youtube credentials

    youtube {
      apiKey = ""
      oauth {
        serviceAccountEmailAddress = ""
        pathToP12KeyFile = ""
      }
    }

When running integration testing, youtube.conf must be in the root of the streams project repository.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

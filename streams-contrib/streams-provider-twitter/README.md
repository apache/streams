streams-provider-twitter
=====================

Connects to the twitter streaming API, collects events, and passes each message downstream.

Example userstream configuration:

    "twitter": {
        host = "api.twitter.com"
        endpoint = "userstream"
        oauth {
            consumerKey = ""
            consumerSecret = ""
            accessToken = ""
            accessTokenSecret = ""
        }
    }
    
Example gardenhose configuration:

    "twitter": {
        host = "api.twitter.com"
        endpoint = "sample"
        oauth {
            consumerKey = ""
            consumerSecret = ""
            accessToken = ""
            accessTokenSecret = ""
        }
        track [
            apache
        ]
        follow [
            TheASF
        ]
        
    }
    

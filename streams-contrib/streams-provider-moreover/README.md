streams-provider-moreover
==========================

Purpose                  

  Module connects to moreover API, collects events, converts to activity, and passes each activity downstream.

Example configuration

    moreover {
        apiKeys {
            key {
                key = ""
                startingSequence = ""
            }
        }
    }
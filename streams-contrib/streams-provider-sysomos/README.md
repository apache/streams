streams-provider-sysomos
==========================

Purpose                  

  Module connects to sysomos API, collects events, converts to activity, and passes each activity downstream.

Example configuration

    sysomos {
        heartbeatIds = [
            HBID
        ]
        apiBatchSize = 500
        apiKey = KEY
        minDelayMs = 10000
        scheduledDelayMs = 120000
        maxBatchSize = 10000
    }
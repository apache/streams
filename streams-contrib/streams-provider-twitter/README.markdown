streams-provider-twitter

Purpose

  Module connects to the twitter streaming API, collects events, and passes each message downstream.

Options

  Sample - supported, tested
  Firehose - supported, not tested
  Site - not currently supported

Capabilities

  Validation

    Optionally, module will validate each message

  Simplification

    Optionally, module can output messages as basic text

  Normalization

    Optionally, module can output messages as other json objects such as Activity

  Deletion

    By default, module will submit delete the object from each directly connected persist step (not implemented)

Run-modes

  Standalone

    Runs in a java process.
    Writes to standard out.

    Placeholder for how
      Configure via property file
      Configure via command line

  Storm

    Runs as a spout.

    Placeholder for how
      Configure via property file
      Configure via command line
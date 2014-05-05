streams-provider-datasift

Purpose

  Module connects to datasift APIs, collects events, and passes each message downstream.

EndPoints

  * Streaming - supported, tested
  * Push - not currently supported

Normalization

  Optionally, module can convert messages to ActivityStreams format

  * Interactions [TwitterJsonTweetActivitySerializer]

[DatasiftInteractionActivitySerializer]: DatasiftInteractionActivitySerializer

  DatasiftInteractionActivitySerializer.class serializes interactions like this:

  ![DatasiftInteractionActivitySerializer.png](DatasiftInteractionActivitySerializer.png)

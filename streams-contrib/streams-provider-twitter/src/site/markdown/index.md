org.apache.streams:streams-provider-twitter
===========================================

streams-provider-twitter contains schema definitions, providers, conversions, and utility classes.

## Data Types

| Schema |
|--------|
| [Tweet.json](com/twitter/tweet.json "Tweet.json") [Tweet.html](apidocs/org/apache/streams/twitter/pojo/Tweet.html "javadoc") |
| [Retweet.json](com/twitter/Retweet.json "Retweet.json") [Retweet.html](apidocs/org/apache/streams/twitter/pojo/Retweet.html "javadoc") |
| [User.json](com/twitter/User.json "User.json") [User.html](apidocs/org/apache/streams/twitter/pojo/User.html "javadoc") |
| [Delete.json](com/twitter/Delete.json "Delete.json") [Delete.html](apidocs/org/apache/streams/twitter/pojo/Delete.html "javadoc") |
| [UserstreamEvent.json](com/twitter/UserstreamEvent.json "UserstreamEvent.json") [UserstreamEvent.html](apidocs/org/apache/streams/twitter/pojo/UserstreamEvent.html "javadoc") |
| [FriendList.json](com/twitter/FriendList.json "FriendList.json") [FriendList.html](apidocs/org/apache/streams/twitter/pojo/FriendList.html "javadoc") |

## Configuration

| Schema |
|--------|
| [TwitterConfiguration.json](com/twitter/tweet.json "TwitterConfiguration.json") [TwitterConfiguration.html](apidocs/org/apache/streams/twitter/TwitterConfiguration.html "javadoc") |

## Components

![components](components.dot.svg "Components")

| Class | Configuration | Example Configuration(s) |
|-------|---------------|--------------------------|
| TwitterUserInformationProvider [TwitterUserInformationProvider.html](apidocs/org/apache/streams/twitter/TwitterUserInformationConfiguration.html "javadoc") | [TwitterUserInformationConfiguration.json](com/twitter/TwitterUserInformationConfiguration.json "TwitterUserInformationConfiguration.json") [TwitterUserInformationConfiguration.html](apidocs/org/apache/streams/twitter/pojo/TwitterUserInformationConfiguration.html "javadoc") | [userinfo.conf](userinfo.conf "userinfo.conf") |
| TwitterTimelineProvider [TwitterTimelineProvider.html](apidocs/org/apache/streams/twitter/TwitterTimelineConfiguration.html "javadoc") | [TwitterUserInformationConfiguration.json](com/twitter/TwitterUserInformationConfiguration.json "TwitterUserInformationConfiguration.json") [TwitterUserInformationConfiguration.html](apidocs/org/apache/streams/twitter/pojo/TwitterUserInformationConfiguration.html "javadoc") | [userinfo.conf](userinfo.conf "userinfo.conf") |
| TwitterStreamProvider [TwitterStreamProvider.html](apidocs/org/apache/streams/twitter/TwitterStreamProvider.html "javadoc") | [TwitterStreamConfiguration.json](com/twitter/TwitterStreamConfiguration.json "TwitterStreamConfiguration.json") [TwitterUserInformationConfiguration.html](apidocs/org/apache/streams/twitter/pojo/TwitterStreamConfiguration.html "javadoc") | [sample.conf](sample.conf "sample.conf")<br/>[userstream.conf](userstream.conf "userstream.conf") |
| TwitterFollowingProvider [TwitterFollowingProvider.html](apidocs/org/apache/streams/twitter/TwitterFollowingConfiguration.html "javadoc") | [TwitterFollowingConfiguration.json](com/twitter/TwitterFollowingConfiguration.json "TwitterFollowingConfiguration.json") [TwitterFollowingConfiguration.html](apidocs/org/apache/streams/twitter/pojo/TwitterFollowingConfiguration.html "javadoc") | [friends.conf](friends.conf "friends.conf")<br/>[followers.conf](followers.conf "followers.conf") |

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0

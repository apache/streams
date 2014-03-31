DEFINE SERIALIZER org.apache.streams.pig.StreamsSerializerExec('org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer');

tweets = LOAD 'src/main/resources/serializertestin.txt' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);

activities = FOREACH tweets GENERATE activityid, source, timestamp, SERIALIZER(object);

STORE activities INTO 'target/tweets-activities';
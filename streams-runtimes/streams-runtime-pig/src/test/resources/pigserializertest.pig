DEFINE SERIALIZER org.apache.streams.pig.StreamsSerializerExec('org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer');

tweets = LOAD 'src/main/resources/serializertestin.txt' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);

activities = FOREACH tweets {
    result = SERIALIZER(object);
    GENERATE activityid, source, timestamp, result;
}

STORE activities INTO 'target/tweets-activities' USING PigStorage('\t');

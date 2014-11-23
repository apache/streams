DEFINE SERIALIZER org.apache.streams.pig.StreamsSerializerExec('org.apache.streams.twitter.serializer.TwitterJsonActivityConverter');
in = LOAD '*' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);
out = FOREACH in {
    result = SERIALIZER(object);
    GENERATE activityid, source, timestamp, result;
}
STORE out INTO 'target' USING PigStorage('\t');

DEFINE PROCESSOR org.apache.streams.pig.StreamsProcessDocumentExec('org.apache.streams.pig.test.DoNothingProcessor');
in = LOAD '*' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);
out = FOREACH in GENERATE activityid, source, timestamp, PROCESSOR(object);
STORE out INTO 'target' USING PigStorage('\t');

DEFINE DONOTHING org.apache.streams.pig.StreamsProcessDocumentExec('org.apache.streams.local.test.processors.DoNothingProcessor');

activities = LOAD '*' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);

unwound = FOREACH activities GENERATE DONOTHING(activityid, source, timestamp, object);

result = FILTER activities BY $3 IS NOT NULL;

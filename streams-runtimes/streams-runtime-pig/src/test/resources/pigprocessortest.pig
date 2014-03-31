DEFINE UNWINDER org.apache.streams.pig.StreamsProcessorExec('org.apache.streams.local.test.processors.DoNothingProcessor');

activities = LOAD '*' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);

unwound = FOREACH activities GENERATE UNWINDER(activityid, source, timestamp, object);

result = FILTER activities BY $3 IS NOT NULL;

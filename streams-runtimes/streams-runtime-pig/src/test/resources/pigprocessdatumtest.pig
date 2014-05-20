DEFINE PROCESSOR org.apache.streams.pig.StreamsProcessDatumExec('org.apache.streams.pig.test.DoNothingProcessor');
in = LOAD '*' USING PigStorage('\t') AS (id: chararray, source: chararray, timestamp: long, object: chararray);
out = FOREACH in GENERATE FLATTEN(PROCESSOR(id, source, timestamp, object));
STORE out INTO 'target' USING PigStorage('\t');

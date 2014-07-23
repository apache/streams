DEFINE PROCESSOR org.apache.streams.pig.StreamsProcessDocumentExec('org.apache.streams.pig.test.AppendStringProcessor', 'doody');
in = LOAD '*' USING PigStorage('\t') AS (id: chararray, source: chararray, timestamp: long, object: chararray);
out = FOREACH in GENERATE id, source, timestamp, PROCESSOR(object);
STORE out INTO 'target' USING PigStorage('\t');

package org.apache.streams.pig.test;

import org.apache.pig.pigunit.PigTest;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.processor.TwitterTypeConverter;
import org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer;
import org.apache.streams.twitter.serializer.TwitterJsonTweetActivitySerializer;
import org.apache.tools.ant.util.StringUtils;
import org.junit.Test;

/**
 * Created by sblackmon on 3/30/14.
 */
public class PigProcessDocumentTest {

    @Test
    public void testPigProcessEmptyDocument() throws Exception {

        String[] input = {
                "159475541894897679\ttwitter,statuses/user_timeline\t1384499359006\t{}"
        };

        DoNothingProcessor processor = new DoNothingProcessor();

        String doc = (String) StringUtils.split(input[0], '\t').get(3);
        StreamsDatum inputDatum = new StreamsDatum(doc);
        inputDatum.setId((String) StringUtils.split(input[0], '\t').get(0));

        processor.prepare(null);

        StreamsDatum resultDatum = processor.process(inputDatum).get(0);
        String resultDocument = (String) resultDatum.getDocument();

        String[] output = new String[1];
        output[0] = "(159475541894897679,twitter,statuses/user_timeline,1384499359006,"+resultDocument+")";

        PigTest test;
        test = new PigTest("src/test/resources/pigprocessdocumenttest.pig");
        test.assertOutput("in", input, "out", output);

    }

    @Test
    public void testPigProcessJsonDocument() throws Exception {

        String[] input = {
                "159475541894897679\ttwitter,statuses/user_timeline\t1384499359006\t{content:\"content\",[\"a\":1,\"b\":\"c\"}"
        };

        DoNothingProcessor processor = new DoNothingProcessor();

        String doc = (String) StringUtils.split(input[0], '\t').get(3);
        StreamsDatum inputDatum = new StreamsDatum(doc);
        inputDatum.setId((String) StringUtils.split(input[0], '\t').get(0));

        processor.prepare(null);

        StreamsDatum resultDatum = processor.process(inputDatum).get(0);
        String resultDocument = (String) resultDatum.getDocument();

        String[] output = new String[1];
        output[0] = "(159475541894897679,twitter,statuses/user_timeline,1384499359006,"+resultDocument+")";

        PigTest test;
        test = new PigTest("src/test/resources/pigprocessdocumenttest.pig");
        test.assertOutput("in", input, "out", output);

    }
}

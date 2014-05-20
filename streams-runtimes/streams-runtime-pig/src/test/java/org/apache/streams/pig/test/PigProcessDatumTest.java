package org.apache.streams.pig.test;

import org.apache.pig.pigunit.PigTest;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.processor.TwitterTypeConverter;
import org.apache.streams.twitter.serializer.TwitterJsonTweetActivitySerializer;
import org.apache.tools.ant.util.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by sblackmon on 3/30/14.
 */
public class PigProcessDatumTest {

    @Test
    public void testPigDoNothingSingleDatum() throws Exception {
        String[] args = {};

        String[] input = {
                "159475541894897679\ttwitter,statuses/user_timeline\t1384499359006\t{content:\"content\",[\"a\":1,\"b\":\"c\"}",
        };

        DoNothingProcessor processor = new DoNothingProcessor();

        String doc = (String) StringUtils.split(input[0], '\t').get(3);
        StreamsDatum inputDatum = new StreamsDatum(doc);
        inputDatum.setId((String) StringUtils.split(input[0], '\t').get(0));

        processor.prepare(null);

        StreamsDatum resultDatum = processor.process(inputDatum).get(0);
        String resultDocument = (String) resultDatum.getDocument();

        String[] output = new String[1];
        output[0] = "(159475541894897679,twitter,statuses/user_timeline,1384499359006," + resultDocument + ")";

        PigTest test;
        test = new PigTest("src/test/resources/pigprocessdatumtest.pig", args);
        test.assertOutput("in", input, "out", output);

    }

    @Test
    public void testPigCopyThriceSingleDatum() throws Exception {
        String[] args = {};

        String[] input = {
                "159475541894897679\ttwitter,statuses/user_timeline\t1384499359006\t{content:\"content\",[\"a\":1,\"b\":\"c\"}",
        };

        CopyThriceProcessor processor = new CopyThriceProcessor();

        String doc = (String) StringUtils.split(input[0], '\t').get(3);
        StreamsDatum inputDatum = new StreamsDatum(doc);
        inputDatum.setId((String) StringUtils.split(input[0], '\t').get(0));

        processor.prepare(null);

        List<StreamsDatum> resultSet = processor.process(inputDatum);

        String[] output = new String[resultSet.size()];

        for( int i = 0; i < output.length; i++ ) {
            output[i] = "(159475541894897679,twitter,statuses/user_timeline,1384499359006," + resultSet.get(i).getDocument() + ")";
        }

        PigTest test;
        test = new PigTest("src/test/resources/pigprocessdatumcopytest.pig", args);
        test.assertOutput("in", input, "out", output);

    }
}

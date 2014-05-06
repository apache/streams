package org.apache.streams.json.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.json.JsonPathExtractor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
* Date: 8/20/13
* Time: 5:57 PM
* To change this template use File | Settings | File Templates.
*/
public class JsonPathExtractorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonPathExtractorTest.class);

    private String testJson;

    @Before
    public void initialize() {
        try {
            testJson = FileUtils.readFileToString(new File("src/test/resources/books.json"));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void test1()
    {
        JsonPathExtractor extractor = new JsonPathExtractor();
        extractor.prepare("$.store.book[*].author");
        List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
        assertThat(result.size(), is(2));
        assertTrue(result.get(0).getDocument() instanceof String);
        assertTrue(result.get(1).getDocument() instanceof String);
    }

    @Ignore
    @Test
    public void test2()
    {
        JsonPathExtractor extractor = new JsonPathExtractor();
        extractor.prepare("$.store.book[?(@.category == 'reference')]");
        List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
        assertThat(result.size(), is(1));
        assertTrue(result.get(0).getDocument() instanceof ObjectNode);
    }

    @Ignore
    @Test
    public void test3()
    {
        JsonPathExtractor extractor = new JsonPathExtractor();
        extractor.prepare("$.store.book[?(@.price > 10)]");
        List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
        assertThat(result.size(), is(1));
        assertTrue(result.get(0).getDocument() instanceof ObjectNode);
    }

    @Ignore
    @Test
    public void test4()
    {
        JsonPathExtractor extractor = new JsonPathExtractor();
        extractor.prepare("$.store.book[?(@.isbn)]");
        List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
        assertThat(result.size(), is(1));
        assertTrue(result.get(0).getDocument() instanceof ObjectNode);
    }

}

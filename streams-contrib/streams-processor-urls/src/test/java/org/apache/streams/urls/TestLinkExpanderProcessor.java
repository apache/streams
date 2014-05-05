package org.apache.streams.urls;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.urls.LinkCrawlerProcessor;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Created by rebanks on 2/27/14.
 */
@Ignore
public class TestLinkExpanderProcessor {

    private static String activityString;

    @Test
    public void testLinkExpanderProcessorNoLinks() {
        testHelper();
    }

    @Test
    public void testLinkExpanderProcessorOneLink() {
        testHelper("http://www.wcgworld.com");
    }

    @Test
    public void testLinkExpanderProcessorMultiLinks() {
        testHelper("http://www.wcgworld.com", "http://www.espn.com");
    }

    private void testHelper(String... urls) {
        LinkCrawlerProcessor processor = new LinkCrawlerProcessor();
        processor.prepare(null);
        for( String url : urls ) {
            assertEquals(1, processor.process(new StreamsDatum(url)).size());
        }
    }

}

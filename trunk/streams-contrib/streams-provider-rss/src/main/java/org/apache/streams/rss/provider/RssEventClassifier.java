package org.apache.streams.rss.provider;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Created by sblackmon on 12/13/13.
 */
public class RssEventClassifier {

    public static Class detectClass( ObjectNode bean ) {
        return SyndEntry.class;
    }
}

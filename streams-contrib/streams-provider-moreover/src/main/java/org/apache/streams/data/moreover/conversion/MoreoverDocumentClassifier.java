package org.apache.streams.data.moreover.conversion;

import com.google.common.base.Preconditions;
import com.moreover.Moreover;
import com.moreover.api.Article;
import org.apache.streams.data.DocumentClassifier;

/**
 * Ensures moreover documents can be converted to Activity
 */
public class MoreoverDocumentClassifier implements DocumentClassifier {
    @Override
    public Class detectClass(Object document) {
        Preconditions.checkArgument(document instanceof String);
        String string = (String) document;
        if( string.startsWith("{") && string.endsWith("}") )
            return Moreover.class;
        else if( string.startsWith("<") && string.endsWith(">") )
            return Article.class;
        return null;
    }
}

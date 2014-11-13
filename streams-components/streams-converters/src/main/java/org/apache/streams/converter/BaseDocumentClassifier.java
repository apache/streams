package org.apache.streams.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import java.io.IOException;

/**
 * Created by sblackmon on 11/12/14.
 */
public class BaseDocumentClassifier implements DocumentClassifier {

    private static BaseDocumentClassifier instance = new BaseDocumentClassifier();

    public static BaseDocumentClassifier getInstance() {
        return instance;
    }

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    @Override
    public Class detectClass(Object document) {
        Preconditions.checkArgument(
                document instanceof String
             || document instanceof ObjectNode);

        Activity activity = null;
        ObjectNode node = null;

        // Soon javax.validation will available in jackson
        //   That will make this simpler and more powerful
        if( document instanceof String ) {
            try {
                activity = this.mapper.readValue((String)document, Activity.class);
                if(ActivityUtil.isValid(activity))
                    return Activity.class;
                else
                    return ObjectNode.class;
            } catch (IOException e1) {
                try {
                    node = this.mapper.readValue((String)document, ObjectNode.class);
                    return ObjectNode.class;
                } catch (IOException e2) {
                    return String.class;
                }
            }
        } else if( document instanceof ObjectNode ){
            activity = this.mapper.convertValue((ObjectNode)document, Activity.class);
            if(ActivityUtil.isValid(activity))
                return Activity.class;
            else
                return ObjectNode.class;
        } else return document.getClass();

    }

}

package org.apache.streams.datasift.provider;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.datasift.Wikipedia;
import org.apache.streams.datasift.blog.Blog;
import org.apache.streams.datasift.board.Board;
import org.apache.streams.datasift.config.Facebook;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.datasift.serializer.StreamsDatasiftMapper;
import org.apache.streams.datasift.twitter.Twitter;
import org.apache.streams.datasift.youtube.YouTube;

import java.io.IOException;

/**
 * Created by sblackmon on 12/13/13.
 */
public class DatasiftEventClassifier {

    public static Class detectClass( String json ) {

        Preconditions.checkNotNull(json);
        Preconditions.checkArgument(StringUtils.isNotEmpty(json));

        ObjectNode objectNode;
        try {
            objectNode = (ObjectNode) StreamsDatasiftMapper.getInstance().readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String type = objectNode.get("interaction").get("type").asText();
        if( type.equals("twitter") )
            return Twitter.class;
        else if( type.equals("youtube") )
            return YouTube.class;
        else if( type.equals("facebook") )
            return Facebook.class;
        else if( type.equals("board") )
            return Board.class;
        else if( type.equals("blog") )
            return Blog.class;
        else if( type.equals("wikipedia") )
            return Wikipedia.class;
        else
            return Interaction.class;
    }
}

package org.apache.streams.datasift.csdl;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ListIterator;

public class DatasiftCsdlUtil {

	private static final Logger log = LoggerFactory
			.getLogger(DatasiftCsdlUtil.class);

	public static String csdlFromTwitterUserIds(List<String> list) throws Exception {
		
		StringBuilder csdlBuilder = new StringBuilder();

        csdlBuilder.append("twitter.user.id in [");
        ListIterator<String> listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("]\n");
        csdlBuilder.append(" OR\n");
        csdlBuilder.append("twitter.in_reply_to_user_id contains_any \"");
        listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(" OR\n");
        csdlBuilder.append("twitter.mention_ids in [");
        listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("]\n");

		log.debug(csdlBuilder.toString());
		
		return csdlBuilder.toString();
	}
	
	public static String csdlFromTwitterUserNames(List<String> list) throws Exception {

        StringBuilder csdlBuilder = new StringBuilder();

        csdlBuilder.append("twitter.user.screen_name contains_any \"");
        ListIterator<String> listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(" OR\n");
        csdlBuilder.append("twitter.in_reply_to_screen_name contains_any \"");
        listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(" OR\n");
        csdlBuilder.append("twitter.mentions contains_any \"");
        listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");

        log.debug(csdlBuilder.toString());

        return csdlBuilder.toString();
	}

    public static String csdlFromKeywords(List<String> include, List<String> exclude) throws Exception {

        StringBuilder csdlBuilder = new StringBuilder();

        csdlBuilder.append("interaction.content contains_any \"");
        ListIterator<String> listIterator = Lists.newArrayList(include).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(" AND NOT ( \n");
        csdlBuilder.append("interaction.content \"");
        listIterator = Lists.newArrayList(exclude).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(")\n");

        log.debug(csdlBuilder.toString());

        return csdlBuilder.toString();
    }
}

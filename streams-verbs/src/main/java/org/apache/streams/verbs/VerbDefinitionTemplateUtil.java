package org.apache.streams.verbs;

import com.google.common.base.Strings;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.stringtemplate.v4.ST;

/**
 * Transforms VerbDefinition templates into readable strings
 */
public class VerbDefinitionTemplateUtil {

    public static String asString(Activity activity, ObjectCombination objectCombination) {

        return asString("*", activity, objectCombination);

    }

    public static String asString(String language, Activity activity, ObjectCombination objectCombination) {

        String template = (String) objectCombination.getTemplates().getAdditionalProperties().get(language);
        template = template.replace('{', '<');
        template = template.replace('}', '>');
        ST st = new ST(template);
        st.add("actor", displayName(activity.getActor()));
        st.add("provider", displayName(activity.getProvider()));
        st.add("object", displayName(activity.getObject()));
        st.add("target", displayName(activity.getTarget()));

        return st.render();
    }

    public static String displayName(ActivityObject activityObject) {
        if( activityObject == null )
            return "";
        if( !Strings.isNullOrEmpty(activityObject.getDisplayName()))
            return activityObject.getDisplayName();
        if( !Strings.isNullOrEmpty(activityObject.getObjectType()))
            return activityObject.getObjectType();
        if( !Strings.isNullOrEmpty(activityObject.toString()))
            return activityObject.toString();
        else return "";
    }
}

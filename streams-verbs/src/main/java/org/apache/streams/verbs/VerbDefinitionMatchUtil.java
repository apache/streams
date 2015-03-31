package org.apache.streams.verbs;

import org.apache.streams.pojo.json.Activity;

import java.util.Set;

/**
 * Check whether an activity matches one or several VerbDefinition
 */
public class VerbDefinitionMatchUtil {

    public static boolean match(Activity activity, Set<VerbDefinition> verbDefinitionSet) {

        for( VerbDefinition verbDefinition : verbDefinitionSet) {
            if( match( activity, verbDefinition )) {
                return true;
            }
        }
        return false;

    }

    public static boolean match(Activity activity, VerbDefinition verbDefinition) {

        if( verbDefinition.getValue() != null &&
            verbDefinition.getValue().equals(activity.getVerb())) {
            for (ObjectCombination objectCombination : verbDefinition.getObjects())
                if (VerbDefinitionResolver.filter(activity, objectCombination) == true)
                    return true;
        }
        return false;
    }

}

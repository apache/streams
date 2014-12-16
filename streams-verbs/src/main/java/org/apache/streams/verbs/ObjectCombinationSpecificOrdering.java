package org.apache.streams.verbs;

import com.google.common.collect.Ordering;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

/**
 * Orders ObjectCombinations from most specific to most general, in context of
 * degree of match to a specified Activity.
 */
public class ObjectCombinationSpecificOrdering extends Ordering<ObjectCombination> {

    private Activity activity;

    public ObjectCombinationSpecificOrdering(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int compare(ObjectCombination left, ObjectCombination right) {
        if( matchCount(left) < matchCount(right))
            return 1;
        if( matchCount(left) > matchCount(right))
            return -1;
        if( !match(activity.getActor(), left.getActor()) && match(activity.getActor(), right.getActor()))
            return 1;
        if( match(activity.getActor(), left.getActor()) && !match(activity.getActor(), right.getActor()))
            return -1;
        if( !match(activity.getObject(), left.getObject()) && match(activity.getObject(), right.getObject()))
            return 1;
        if( match(activity.getObject(), left.getObject()) && !match(activity.getObject(), right.getObject()))
            return -1;
        if( !match(activity.getTarget(), left.getTarget()) && match(activity.getTarget(), right.getTarget()))
            return 1;
        if( match(activity.getTarget(), left.getTarget()) && !match(activity.getTarget(), right.getTarget()))
            return -1;
        if( !match(activity.getProvider(), left.getProvider()) && match(activity.getTarget(), right.getProvider()))
            return 1;
        if( match(activity.getProvider(), left.getProvider()) && !match(activity.getTarget(), right.getProvider()))
            return -1;
        return 0;
    }

    public int matchCount(ObjectCombination objectCombination) {
        int matchCount = 0;
        if( match(activity.getActor(), objectCombination.getActor()))
            matchCount++;
        if( match(activity.getObject(), objectCombination.getObject()))
            matchCount++;
        if( match(activity.getTarget(), objectCombination.getTarget()))
            matchCount++;
        if( match(activity.getProvider(), objectCombination.getProvider()))
            matchCount++;
        return matchCount;
    }

    public boolean match(ActivityObject activityObject, String pattern) {
        if( activityObject != null &&
            activityObject.getObjectType() != null &&
            activityObject.getObjectType().equals(pattern)) return true;
        else return false;
    }
}

package org.apache.streams.verbs;

import com.google.common.collect.Ordering;

/**
 * Orders ObjectCombinations from most specific to most general, without regard
 * for degree of match to any specific Activity.
 */
public class ObjectCombinationGenericOrdering extends Ordering<ObjectCombination> {

    public ObjectCombinationGenericOrdering() {}

    @Override
    public int compare(ObjectCombination left, ObjectCombination right) {
        if( wildcardCount(left) < wildcardCount(right))
            return -1;
        if( wildcardCount(left) > wildcardCount(right))
            return 1;
        if( !wildcard(left.getActor()) && wildcard(right.getActor()))
            return -1;
        if( wildcard(left.getActor()) && !wildcard(right.getActor()))
            return 1;
        if( !wildcard(left.getObject()) && wildcard(right.getObject()))
            return -1;
        if( wildcard(left.getObject()) && !wildcard(right.getObject()))
            return 1;
        if( !wildcard(left.getTarget()) && wildcard(right.getTarget()))
            return -1;
        if( wildcard(left.getTarget()) && !wildcard(right.getTarget()))
            return 1;
        if( !wildcard(left.getProvider()) && wildcard(right.getProvider()))
            return -1;
        if( wildcard(left.getProvider()) && !wildcard(right.getProvider()))
            return 1;
        return 0;
    }

    public int wildcardCount(ObjectCombination objectCombination) {
        int wildcardCount = 0;
        if( wildcard(objectCombination.getActor()))
            wildcardCount++;
        if( wildcard(objectCombination.getObject()))
            wildcardCount++;
        if( wildcard(objectCombination.getTarget()))
            wildcardCount++;
        if( wildcard(objectCombination.getProvider()))
            wildcardCount++;
        return wildcardCount;
    }

    public boolean wildcard(String pattern) {
        if( pattern.equals("*")) return true;
        else return false;
    }
}

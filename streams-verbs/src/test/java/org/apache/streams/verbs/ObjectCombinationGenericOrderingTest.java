package org.apache.streams.verbs;

import org.junit.Test;

/**
 * Orders ObjectCombinations from most specific to most general, without regard
 * for degree of match to any specific Activity.
 */
public class ObjectCombinationGenericOrderingTest  {

    @Test
    public void compareWildcardCountTest() {
        ObjectCombination combination1 = new ObjectCombination();
        ObjectCombination combination2 = new ObjectCombination().withActor("actor");
        assert (new ObjectCombinationGenericOrdering()).compare(combination1, combination2) > 0;
        ObjectCombination combination3 = new ObjectCombination();
        ObjectCombination combination4 = new ObjectCombination().withProvider("provider");
        assert (new ObjectCombinationGenericOrdering()).compare(combination3, combination4) > 0;
    }


}

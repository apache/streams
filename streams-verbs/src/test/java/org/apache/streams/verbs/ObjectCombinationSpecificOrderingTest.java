package org.apache.streams.verbs;

import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.junit.Test;

/**
 * Orders ObjectCombinations from most specific to most general, without regard
 * for degree of match to any specific Activity.
 */
public class ObjectCombinationSpecificOrderingTest {

    @Test
    public void compareMatchCountTest() {
        Actor actor = new Actor();
        actor.setObjectType("actor");
        Activity activity = new Activity().withActor(actor);
        ObjectCombination combination1 = new ObjectCombination();
        ObjectCombination combination2 = new ObjectCombination().withActor("actor");
        assert (new ObjectCombinationSpecificOrdering(activity)).compare(combination1, combination2) > 0;
        Provider provider = new Provider();
        provider.setObjectType("application");
        Activity activity2 = new Activity().withProvider(provider);
        ObjectCombination combination3 = new ObjectCombination();
        ObjectCombination combination4 = new ObjectCombination().withProvider("provider");
        assert (new ObjectCombinationSpecificOrdering(activity2)).compare(combination3, combination4) > 0;
    }


}

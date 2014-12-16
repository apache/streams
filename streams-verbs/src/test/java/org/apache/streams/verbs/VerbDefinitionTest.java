package org.apache.streams.verbs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class VerbDefinitionTest {

    ObjectMapper mapper = new ObjectMapper();

    /**
     * Test read verb definition from json
     */
    @Test
    public void testReadVerbDefinitionJson() throws Exception {

        VerbDefinition definition = mapper.readValue(VerbDefinitionTest.class.getResourceAsStream("/do.json"), VerbDefinition.class);

        assert definition != null;
        assert definition.getObjectType().equals("verb");
        assert definition.getObjects().size() == 1;
        assert definition.getObjects().get(0).getActor().equals("*");
        assert definition.getObjects().get(0).getObject().equals("*");
        assert definition.getObjects().get(0).getTarget().equals("*");
        assert definition.getObjects().get(0).getProvider().equals("*");
        assert definition.getObjects().get(0).getTemplates().getAdditionalProperties().size() == 1;
    }

    /**
     * Test verb definition defaults are set
     */
    @Test
    public void testObjectCombinationDefaults() throws Exception {

        ObjectCombination combination = new ObjectCombination();

        assert combination.getActor().equals("*");
        assert combination.getObject().equals("*");
        assert combination.getTarget().equals("*");
        assert combination.getProvider().equals("*");
        assert combination.getTargetRequired() == false;

    }

}

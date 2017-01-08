package org.apache.streams.util.schema.test;

import org.apache.streams.util.PropertyUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

/**
 * Created by sblackmon on 1/8/17.
 */
public class PropertyUtilTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  String flatJson = "{\"a.a\": \"aa\", \"a.b\": \"ab\", \"b.a\": \"ba\", \"b.b\": \"bb\"}";

  @Test
  public void testUnflattenObjectNode() throws Exception {
    ObjectNode flatNode = mapper.readValue(flatJson, ObjectNode.class);
    ObjectNode unflattenedNode = PropertyUtil.unflattenObjectNode(flatNode, '.');
    assert(unflattenedNode.size() == 2);
  }
}


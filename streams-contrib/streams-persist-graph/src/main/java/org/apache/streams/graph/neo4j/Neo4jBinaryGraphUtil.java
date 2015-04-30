package org.apache.streams.graph.neo4j;

import com.google.common.collect.Lists;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Neo4jBinaryGraphUtil {

    CypherQueryGraphHelper queryGraphHelper = new CypherQueryGraphHelper();

    protected void addUniqueIndex(GraphDatabaseService graph, String label, String property, Boolean wait ) {
        Transaction tx = graph.beginTx();
        try {
            Schema schema = graph.schema();
            schema
                .constraintFor(DynamicLabel.label(label))
                .assertPropertyIsUnique(property)
                .create();
            if (wait)
                schema.awaitIndexesOnline(2L, TimeUnit.HOURS);
            tx.success();
            tx.close();
        } finally {
            tx.failure();
            tx.close();
        }
    }

    protected void addIndex(GraphDatabaseService graph, String label, String property, Boolean wait) {
        Transaction tx = graph.beginTx();
        try {
            Schema schema = graph.schema();
            schema
                .indexFor(DynamicLabel.label(label))
                .on(property)
                .create();
            if (wait)
                schema.awaitIndexesOnline(2L, TimeUnit.HOURS);
            tx.success();
            tx.close();
        } finally {
            tx.failure();
            tx.close();
        }
    }


    protected void addNode(GraphDatabaseService graph, List<String> labelStringList, Pair<String, Map<String, Object>> nodeIdPlusProperties) {
        Transaction tx = graph.beginTx();
        List<Label> labelList = Lists.newArrayList();
        for( String labelString : labelStringList ) {
            labelList.add(DynamicLabel.label(labelString));
        }
        try {
            Node node = graph.createNode(labelList.toArray(new Label[0]));
            node.setProperty("id", nodeIdPlusProperties.getValue0());
            for( Map.Entry<String, Object> property : nodeIdPlusProperties.getValue1().entrySet()) {
                node.setProperty(property.getKey(), property.getValue());
            }
            tx.success();
        } catch(Exception e) {
            tx.failure();
        } finally {
            tx.close();
        }
    }

    protected void addRelationship(GraphDatabaseService graph, List<String> labelStringList, Quartet<String, String, String, Map<String, Object>> relationshipIdsPlusProperties) {
        Transaction tx = graph.beginTx();
        try {
            Node source = graph.findNodesByLabelAndProperty(DynamicLabel.label(labelStringList.get(0)), "id", relationshipIdsPlusProperties.getValue0()).iterator().next();
            Node destination = graph.findNodesByLabelAndProperty(DynamicLabel.label(labelStringList.get(0)), "id", relationshipIdsPlusProperties.getValue1()).iterator().next();
            source.createRelationshipTo(destination, DynamicRelationshipType.withName(labelStringList.get(labelStringList.size() - 1)));
            tx.success();
        } catch(Exception e) {
            tx.failure();
        } finally {
            tx.close();
        }
    }

}

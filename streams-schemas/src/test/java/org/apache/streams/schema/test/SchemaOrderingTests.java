package org.apache.streams.schema.test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.streams.schema.Schema;
import org.apache.streams.schema.SchemaStore;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sblackmon on 5/3/16.
 */
public class SchemaOrderingTests {

    @Test
    public void compareVerbParent() {
        SchemaStore schemaStore = new SchemaStore();
        File update = new File("target/classes/verbs/update.json");
        schemaStore.create(update.toURI());
        File activity = new File("target/classes/activity.json");
        schemaStore.create(activity.toURI());
        assert( schemaStore.compare( schemaStore.getByUri(update.toURI()).get(), schemaStore.getByUri(activity.toURI()).get()) == 1);
        Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
        assertContainsItemsEndingWithInOrder(
                schemaIterator,
                Lists.newArrayList(
                        "activity.json",
                        "update.json"
                )
        );
    }

    @Test
    public void compareObjectTypeParent() {
        SchemaStore schemaStore = new SchemaStore();
        File alert = new File("target/classes/objectTypes/alert.json");
        schemaStore.create(alert.toURI());
        File object = new File("target/classes/object.json");
        schemaStore.create(object.toURI());
        assert( schemaStore.compare( schemaStore.getByUri(object.toURI()).get(), schemaStore.getByUri(alert.toURI()).get()) == -1);
        Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
        assertContainsItemsEndingWithInOrder(
                schemaIterator,
                Lists.newArrayList(
                        "object.json",
                        "alert.json"
                )
        );
    }

    @Test
    public void compareUnrelated() {
        SchemaStore schemaStore = new SchemaStore();
        File alert = new File("target/classes/objectTypes/alert.json");
        schemaStore.create(alert.toURI());
        File update = new File("target/classes/verbs/update.json");
        schemaStore.create(update.toURI());
        assert( schemaStore.compare( schemaStore.getByUri(alert.toURI()).get(), schemaStore.getByUri(update.toURI()).get()) == 0);
    }

    @Test
    public void compareVerbFieldRef() {
        SchemaStore schemaStore = new SchemaStore();
        File update = new File("target/classes/verbs/update.json");
        schemaStore.create(update.toURI());
        File object = new File("target/classes/object.json");
        schemaStore.create(object.toURI());
        assert( schemaStore.compare( schemaStore.getByUri(update.toURI()).get(), schemaStore.getByUri(object.toURI()).get()) == 1);
        Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
        assertContainsItemsEndingWithInOrder(
                schemaIterator,
                Lists.newArrayList(
                        "object.json",
                        "update.json"
                )
        );
    }

    @Test
    public void compareObjectTypeFieldRef() {
        SchemaStore schemaStore = new SchemaStore();
        File alert = new File("target/classes/objectTypes/alert.json");
        schemaStore.create(alert.toURI());
        File media_link = new File("target/classes/media_link.json");
        schemaStore.create(media_link.toURI());
        assert( schemaStore.compare( schemaStore.getByUri(media_link.toURI()).get(), schemaStore.getByUri(alert.toURI()).get()) == -1);
        Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
        assertContainsItemsEndingWithInOrder(
                schemaIterator,
                Lists.newArrayList(
                        "media_link.json",
                        "object.json",
                        "alert.json"
                )
        );
    }

    @Test
    public void compareVerbAncestorIndirect() {
        SchemaStore schemaStore = new SchemaStore();
        File update = new File("target/classes/verbs/update.json");
        schemaStore.create(update.toURI());
        File media_link = new File("target/classes/media_link.json");
        assert( schemaStore.compare( schemaStore.getByUri(media_link.toURI()).get(), schemaStore.getByUri(update.toURI()).get()) == -1);
        Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
        assertContainsItemsEndingWithInOrder(
                schemaIterator,
                Lists.newArrayList(
                        "media_link.json",
                        "update.json"
                )
        );
    }


    public void assertContainsItemsEndingWithInOrder(Iterator<Schema> iterator, List<String> items) {
        for( String item : items ) {
            Optional<Schema> tryFind = Iterators.tryFind( iterator, new SchemaUriEndsWithPredicate(item) );
            assert( tryFind.isPresent() );
        }
    }

    public class SchemaUriEndsWithPredicate implements Predicate<Schema> {

        private String endsWith;

        public SchemaUriEndsWithPredicate(String endsWith) {
            this.endsWith = endsWith;
        }

        @Override
        public boolean apply(Schema input) {
            return input.getURI().getPath().endsWith(endsWith);
        }

        @Override
        public boolean equals(Object object) {
            return false;
        }
    }
}

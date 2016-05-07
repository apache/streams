package org.apache.streams.schema.test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.streams.schema.Schema;
import org.apache.streams.schema.SchemaStore;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sblackmon on 5/2/16.
 */
public class SchemaStoreTests {

    @Test
    public void indexMediaLink() {
        SchemaStore schemaStore = new SchemaStore();
        File file = new File("target/classes/media_link.json");
        schemaStore.create(file.toURI());
        assert( schemaStore.getFileUriCount() == 1);
        assert( schemaStore.getByUri(file.toURI()).isPresent());
        assert( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
    }

    @Test
    public void indexApprove() {
        SchemaStore schemaStore = new SchemaStore();
        File file = new File("target/classes/verbs/approve.json");
        schemaStore.create(file.toURI());
        assert( schemaStore.getFileUriCount() == 4);
        assert( schemaStore.getByUri(file.toURI()).isPresent());
        assert( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
    }

    @Test
    public void indexCollection() {
        SchemaStore schemaStore = new SchemaStore();
        File file = new File("target/classes/collection.json");
        schemaStore.create(file.toURI());
        assert( schemaStore.getFileUriCount() == 3);
        assert( schemaStore.getByUri(file.toURI()).isPresent());
        assert( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
        Schema collection = schemaStore.getByUri(file.toURI()).get();
        assert( collection.getParent() == null );
        assert( schemaStore.getById(
                URI.create("http://streams.incubator.apache.org/site/0.3-incubating-SNAPSHOT/streams-schemas/object.json#"
                )).isPresent());
    }

    @Test
    public void indexUpdate() {
        SchemaStore schemaStore = new SchemaStore();
        File file = new File("target/classes/verbs/update.json");
        schemaStore.create(file.toURI());
        assert( schemaStore.getFileUriCount() == 4);
        assert( schemaStore.getByUri(file.toURI()).isPresent());
        assert( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
        Schema update = schemaStore.getByUri(file.toURI()).get();
        assert( update.getParent() != null );
        assert( update.getParent().getId().getScheme().equals("http"));
        assert( update.getParent().getId().getHost().equals("streams.incubator.apache.org"));
        assert( update.getParent().getId().getPath().startsWith("/site/0.3-incubating-SNAPSHOT/streams-schemas"));
        assert( update.getParent().getId().getPath().endsWith("activity.json"));
    }

    // test create from messed up URI

    // test create from URI with messed up reference

}

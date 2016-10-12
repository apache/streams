package org.apache.streams.util.schema.test;

import org.apache.streams.util.schema.Schema;
import org.apache.streams.util.schema.SchemaStore;
import org.apache.streams.util.schema.SchemaStoreImpl;
import org.junit.Test;

import java.io.File;
import java.net.URI;

/**
 * Created by sblackmon on 5/2/16.
 */
public class SchemaStoreTest {

    @Test
    public void indexMediaLink() {
        SchemaStore schemaStore = new SchemaStoreImpl();
        File file = new File("target/test-classes/media_link.json");
        schemaStore.create(file.toURI());
        assert( schemaStore.getFileUriCount() == 1);
        assert( schemaStore.getByUri(file.toURI()).isPresent());
        assert( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
    }

    @Test
    public void indexApprove() {
        SchemaStore schemaStore = new SchemaStoreImpl();
        File file = new File("target/test-classes/verbs/approve.json");
        schemaStore.create(file.toURI());
        assert( schemaStore.getFileUriCount() == 4);
        assert( schemaStore.getByUri(file.toURI()).isPresent());
        assert( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
    }

    @Test
    public void indexCollection() {
        SchemaStore schemaStore = new SchemaStoreImpl();
        File file = new File("target/test-classes/collection.json");
        schemaStore.create(file.toURI());
        assert( schemaStore.getFileUriCount() == 3);
        assert( schemaStore.getByUri(file.toURI()).isPresent());
        assert( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
        Schema collection = schemaStore.getByUri(file.toURI()).get();
        assert( collection.getParent() == null );
        assert( schemaStore.getById(
                URI.create("http://streams.incubator.apache.org/site/latest/streams-project/streams-schemas/object.json#"
                )).isPresent());
    }

    @Test
    public void indexUpdate() {
        SchemaStore schemaStore = new SchemaStoreImpl();
        File file = new File("target/test-classes/verbs/update.json");
        schemaStore.create(file.toURI());
        assert( schemaStore.getFileUriCount() == 4);
        assert( schemaStore.getByUri(file.toURI()).isPresent());
        assert( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
        Schema update = schemaStore.getByUri(file.toURI()).get();
        assert( update.getParent() != null );
        assert( update.getParent().getId().getScheme().equals("http"));
        assert( update.getParent().getId().getHost().equals("streams.incubator.apache.org"));
        assert( update.getParent().getId().getPath().startsWith("/site/latest/streams-project/streams-schemas"));
        assert( update.getParent().getId().getPath().endsWith("activity.json"));
    }

    // test create from messed up URI

    // test create from URI with messed up reference

}

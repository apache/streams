package org.apache.streams.util.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;

import java.net.URI;
import java.util.Comparator;
import java.util.Iterator;

/**
 * A SchemaStore resolves and indexes json schemas and makes their properties available.
 *
 * Implementations include
 * - SchemaStoreImpl
 */
public interface SchemaStore extends Comparator<Schema> {
    
    Schema create(URI uri);

    Schema create(Schema parent, String path);

    void clearCache();

    Integer getSize();

    Optional<Schema> getById(URI id);

    Optional<Schema> getByUri(URI uri);

    Integer getFileUriCount();

    Integer getHttpUriCount();

    Iterator<Schema> getSchemaIterator();

    ObjectNode resolveProperties(Schema schema, ObjectNode fieldNode, String resourceId);

    ObjectNode resolveItems(Schema schema, ObjectNode fieldNode, String resourceId);

    @Override
    int compare(Schema left, Schema right);
}

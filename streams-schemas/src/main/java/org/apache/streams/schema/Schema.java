package org.apache.streams.schema;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;

/**
 * A JSON Schema document.
 */
public class Schema {

    private final URI id;
    private final URI uri;
    private final JsonNode content;
    private final Schema parent;
    private final boolean generate;

    public Schema(URI uri, JsonNode content, Schema parent, boolean generate) {
        this.uri = uri;
        this.content = content;
        this.parent = parent;
        this.generate = generate;
        this.id = content.has("id") ? URI.create(content.get("id").asText()) : null;
    }

    public URI getId() {
        return id;
    }

    public URI getURI() {
        return uri;
    }

    public JsonNode getContent() {
        return content;
    }

    public JsonNode getParentContent() {
        if( parent != null )
            return parent.getContent();
        else return null;
    }

    public URI getParentURI() {
        if( parent != null ) return parent.getURI();
        else return null;
    }

    public boolean isGenerated() {
        return generate;
    }

    public Schema getParent() {
        return parent;
    }

}
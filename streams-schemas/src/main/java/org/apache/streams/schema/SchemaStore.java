package org.apache.streams.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.ContentResolver;
import org.jsonschema2pojo.FragmentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.streams.schema.URIUtil.safeResolve;

/**
 * Created by steve on 4/30/16.
 */
public class SchemaStore extends Ordering<Schema> {

    private final static Logger LOGGER = LoggerFactory.getLogger(SchemaStore.class);
    private final static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    protected Map<URI, Schema> schemas = new HashMap();
    protected FragmentResolver fragmentResolver = new FragmentResolver();
    protected ContentResolver contentResolver = new ContentResolver();

    public SchemaStore() {
    }

    public synchronized Schema create(URI uri) {
        if(!getByUri(uri).isPresent()) {
            URI baseURI = URIUtil.removeFragment(uri);
            JsonNode baseNode = this.contentResolver.resolve(baseURI);
            if(uri.toString().contains("#") && !uri.toString().endsWith("#")) {
                Schema newSchema = new Schema(baseURI, baseNode, null, true);
                this.schemas.put(baseURI, newSchema);
                JsonNode childContent = this.fragmentResolver.resolve(baseNode, '#' + StringUtils.substringAfter(uri.toString(), "#"));
                this.schemas.put(uri, new Schema(uri, childContent, newSchema, false));
            } else {
                if( baseNode.has("extends") && baseNode.get("extends").isObject()) {
                    URI ref = URI.create(((ObjectNode)baseNode.get("extends")).get("$ref").asText());
                    URI absoluteURI;
                    if( ref.isAbsolute())
                        absoluteURI = ref;
                    else
                        absoluteURI = baseURI.resolve(ref);
                    JsonNode parentNode = this.contentResolver.resolve(absoluteURI);
                    Schema parentSchema = null;
                    if( this.schemas.get(absoluteURI) != null ) {
                        parentSchema = this.schemas.get(absoluteURI);
                    } else {
                        parentSchema = create(absoluteURI);
                    }
                    this.schemas.put(uri, new Schema(uri, baseNode, parentSchema, true));
                } else {
                    this.schemas.put(uri, new Schema(uri, baseNode, null, true));
                }
            }
            List<JsonNode> refs = baseNode.findValues("$ref");
            for( JsonNode ref : refs ) {
                if( ref.isValueNode() ) {
                    String refVal = ref.asText();
                    URI refURI = null;
                    try {
                        refURI = URI.create(refVal);
                    } catch( Exception e ) {
                        LOGGER.info("Exception: {}", e.getMessage());
                    }
                    if (refURI != null && !getByUri(refURI).isPresent()) {
                        if (refURI.isAbsolute())
                            create(refURI);
                        else
                            create(baseURI.resolve(refURI));
                    }
                }
            }
        }

        return this.schemas.get(uri);
    }

    public Schema create(Schema parent, String path) {
        if(path.equals("#")) {
            return parent;
        } else {
            path = StringUtils.stripEnd(path, "#?&/");
            URI id = parent != null && parent.getId() != null?parent.getId().resolve(path):URI.create(path);
            if(this.selfReferenceWithoutParentFile(parent, path)) {
                this.schemas.put(id, new Schema(id, this.fragmentResolver.resolve(parent.getParentContent(), path), parent, false));
                return this.schemas.get(id);
            } else {
                return this.create(id);
            }
        }
    }

    protected boolean selfReferenceWithoutParentFile(Schema parent, String path) {
        return parent != null && (parent.getId() == null || parent.getId().toString().startsWith("#/")) && path.startsWith("#/");
    }

    public synchronized void clearCache() {
        this.schemas.clear();
    }

    public Integer getSize() {
        return schemas.size();
    }

    public Optional<Schema> getById(URI id) {
        for( Schema schema : schemas.values() ) {
            if( schema.getId() != null && schema.getId().equals(id) )
                return Optional.of(schema);
        }
        return Optional.absent();
    }

    public Optional<Schema> getByUri(URI uri) {
        for( Schema schema : schemas.values() ) {
            if( schema.getURI().equals(uri) )
                return Optional.of(schema);
        }
        return Optional.absent();
    }

    public Integer getFileUriCount() {
        int count = 0;
        for( Schema schema : schemas.values() ) {
            if( schema.getURI().getScheme().equals("file") )
                count++;
        }
        return count;
    }

    public Integer getHttpUriCount() {
        int count = 0;
        for( Schema schema : schemas.values() ) {
            if( schema.getURI().getScheme().equals("http") )
                count++;
        }
        return count;
    }

    public Iterator<Schema> getSchemaIterator() {
        List<Schema> schemaList = Lists.newArrayList(schemas.values());
        Collections.sort(schemaList, this);
        return schemaList.iterator();
    }

    public ObjectNode resolveProperties(Schema schema, ObjectNode fieldNode, String resourceId) {
        // this should return something more suitable like:
        //   Map<String, Pair<Schema, ObjectNode>>
        ObjectNode schemaProperties = NODE_FACTORY.objectNode();
        ObjectNode parentProperties = NODE_FACTORY.objectNode();
        if (fieldNode == null) {
            ObjectNode schemaContent = (ObjectNode) schema.getContent();
            if( schemaContent.has("properties") ) {
                schemaProperties = (ObjectNode) schemaContent.get("properties");
                if (schema.getParentContent() != null) {
                    ObjectNode parentContent = (ObjectNode) schema.getParentContent();
                    if (parentContent.has("properties")) {
                        parentProperties = (ObjectNode) parentContent.get("properties");
                    }
                }
            }
        } else if (fieldNode != null && fieldNode.size() > 0) {
            if( fieldNode.has("properties") && fieldNode.get("properties").isObject() && fieldNode.get("properties").size() > 0 )
                schemaProperties = (ObjectNode) fieldNode.get("properties");
            URI parentURI = null;
            if( fieldNode.has("$ref") || fieldNode.has("extends") ) {
                JsonNode refNode = fieldNode.get("$ref");
                JsonNode extendsNode = fieldNode.get("extends");
                if (refNode != null && refNode.isValueNode())
                    parentURI = URI.create(refNode.asText());
                else if (extendsNode != null && extendsNode.isObject())
                    parentURI = URI.create(extendsNode.get("$ref").asText());
                ObjectNode parentContent = null;
                URI absoluteURI;
                if (parentURI.isAbsolute())
                    absoluteURI = parentURI;
                else {
                    absoluteURI = schema.getURI().resolve(parentURI);
                    if (!absoluteURI.isAbsolute() || (absoluteURI.isAbsolute() && !getByUri(absoluteURI).isPresent() ))
                        absoluteURI = schema.getParentURI().resolve(parentURI);
                }
                if (absoluteURI != null && absoluteURI.isAbsolute()) {
                    if (getByUri(absoluteURI).isPresent())
                        parentContent = (ObjectNode) getByUri(absoluteURI).get().getContent();
                    if (parentContent != null && parentContent.isObject() && parentContent.has("properties")) {
                        parentProperties = (ObjectNode) parentContent.get("properties");
                    } else if (absoluteURI.getPath().endsWith("#properties")) {
                        absoluteURI = URI.create(absoluteURI.toString().replace("#properties", ""));
                        parentProperties = (ObjectNode) getByUri(absoluteURI).get().getContent().get("properties");
                    }
                }
            }


        }

        ObjectNode resolvedProperties = NODE_FACTORY.objectNode();
        if (parentProperties != null && parentProperties.size() > 0)
            resolvedProperties = SchemaUtil.mergeProperties(schemaProperties, parentProperties);
        else resolvedProperties = schemaProperties.deepCopy();

        return resolvedProperties;
    }

    @Override
    public int compare(Schema left, Schema right) {
        // are they the same?
        if( left.equals(right)) return 0;
        // is one an ancestor of the other
        Schema candidateAncestor = left;
        while( candidateAncestor.getParent() != null ) {
            candidateAncestor = candidateAncestor.getParent();
            if( candidateAncestor.equals(right))
                return 1;
        }
        candidateAncestor = right;
        while( candidateAncestor.getParent() != null ) {
            candidateAncestor = candidateAncestor.getParent();
            if( candidateAncestor.equals(left))
                return -1;
        }
        // does one have a field that reference the other?
        for( JsonNode refNode : left.getContent().findValues("$ref") ) {
            String refText = refNode.asText();
            Optional<URI> resolvedURI = safeResolve(left.getURI(), refText);
            if( resolvedURI.isPresent() && resolvedURI.get().equals(right.getURI()))
                return 1;
        }
        for( JsonNode refNode : right.getContent().findValues("$ref") ) {
            String refText = refNode.asText();
            Optional<URI> resolvedURI = safeResolve(right.getURI(), refText);
            if( resolvedURI.isPresent() && resolvedURI.get().equals(left.getURI()))
                return -1;
        }
        // does one have a field that reference a third schema that references the other?
        for( JsonNode refNode : left.getContent().findValues("$ref") ) {
            String refText = refNode.asText();
            Optional<URI> possibleConnectorURI = safeResolve(left.getURI(), refText);
            if( possibleConnectorURI.isPresent()) {
                Optional<Schema> possibleConnector = getByUri(possibleConnectorURI.get());
                if (possibleConnector.isPresent()) {
                    for (JsonNode connectorRefNode : possibleConnector.get().getContent().findValues("$ref")) {
                        String connectorRefText = connectorRefNode.asText();
                        Optional<URI> resolvedURI = safeResolve(possibleConnector.get().getURI(), connectorRefText);
                        if (resolvedURI.isPresent() && resolvedURI.get().equals(right.getURI()))
                            return 1;
                    }
                }
            }
        }
        for( JsonNode refNode : right.getContent().findValues("$ref") ) {
            String refText = refNode.asText();
            Optional<URI> possibleConnectorURI = safeResolve(right.getURI(), refText);
            if( possibleConnectorURI.isPresent()) {
                Optional<Schema> possibleConnector = getByUri(possibleConnectorURI.get());
                if (possibleConnector.isPresent()) {
                    for (JsonNode connectorRefNode : possibleConnector.get().getContent().findValues("$ref")) {
                        String connectorRefText = connectorRefNode.asText();
                        Optional<URI> resolvedURI = safeResolve(possibleConnector.get().getURI(), connectorRefText);
                        if (resolvedURI.isPresent() && resolvedURI.get().equals(left.getURI()))
                            return -1;
                    }
                }
            }
        }
        return 0;
    }

}


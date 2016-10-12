package org.apache.streams.util.schema;

/**
 * FieldType defines xsd types that streams schema processing libraries should
 * be able to translate.
 */
public enum FieldType {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    OBJECT,
    ARRAY
}

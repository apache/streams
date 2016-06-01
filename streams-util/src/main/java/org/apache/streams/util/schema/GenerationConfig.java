package org.apache.streams.util.schema;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.Iterator;

/**
 * GenerationConfig represents the common fields and field accessors for
 * streams modules that transform schemas into generated-sources or generated-resources
 */
public interface GenerationConfig {

    /**
     * Gets the 'source' configuration option.
     *
     * @return The source file(s) or directory(ies) from which JSON Schema will
     *         be read.
     */
    Iterator<URL> getSource();

    /**
     * Gets the 'targetDirectory' configuration option.
     *
     * @return The target directory into which generated types will be written
     *         (may or may not exist before types are written)
     */
    File getTargetDirectory();

    /**
     * Gets the 'outputEncoding' configuration option.
     *
     * @return The character encoding that should be used when writing output files.
     */
    String getOutputEncoding();

    /**
     * Gets the file filter used to isolate the schema mapping files in the
     * source directories.
     *
     * @return the file filter use when scanning for schema files.
     */
    FileFilter getFileFilter();

    /**
     * Gets the 'includeAdditionalProperties' configuration option.
     *
     * @return Whether to allow 'additional properties' support in objects.
     *         Setting this to false will disable additional properties support,
     *         regardless of the input schema(s).
     */
//    boolean isIncludeAdditionalProperties();

    /**
     * Gets the 'targetVersion' configuration option.
     *
     *  @return The target version for generated source files.
     */
//    String getTargetVersion();

//    /**
//     * Gets the `includeDynamicAccessors` configuraiton option.
//     *
//     * @return Whether to include dynamic getters, setters, and builders
//     *         or to omit these methods.
//     */
//    boolean isIncludeDynamicAccessors();

//    /**
//     * Gets the `dateTimeType` configuration option.
//     *         <p>
//     *         Example values:
//     *         <ul>
//     *         <li><code>org.joda.time.LocalDateTime</code> (Joda)</li>
//     *         <li><code>java.time.LocalDateTime</code> (JSR310)</li>
//     *         <li><code>null</code> (default behavior)</li>
//     *         </ul>
//     *
//     * @return The java type to use instead of {@link java.util.Date}
//     *         when adding date type fields to generate Java types.
//     */
//    String getDateTimeType();
//
//    /**
//     * Gets the `dateType` configuration option.
//     *         <p>
//     *         Example values:
//     *         <ul>
//     *         <li><code>org.joda.time.LocalDate</code> (Joda)</li>
//     *         <li><code>java.time.LocalDate</code> (JSR310)</li>
//     *         <li><code>null</code> (default behavior)</li>
//     *         </ul>
//     *
//     * @return The java type to use instead of string
//     *         when adding string type fields with a format of date (not
//     *         date-time) to generated Java types.
//     */
//    String getDateType();
//
//    /**
//     * Gets the `timeType` configuration option.
//     *         <p>
//     *         Example values:
//     *         <ul>
//     *         <li><code>org.joda.time.LocalTime</code> (Joda)</li>
//     *         <li><code>java.time.LocalTime</code> (JSR310)</li>
//     *         <li><code>null</code> (default behavior)</li>
//     *         </ul>
//     *
//     * @return The java type to use instead of string
//     *         when adding string type fields with a format of time (not
//     *         date-time) to generated Java types.
//     */
//    String getTimeType();

}

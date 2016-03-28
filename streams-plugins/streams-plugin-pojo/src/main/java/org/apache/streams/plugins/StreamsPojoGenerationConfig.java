package org.apache.streams.plugins;

import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by sblackmon on 3/27/16.
 */
public class StreamsPojoGenerationConfig implements GenerationConfig {

        @Override
        public boolean isGenerateBuilders() {
            return false;
        }

        @Override
        public boolean isUsePrimitives() {
            return false;
        }

        @Override
        public Iterator<URL> getSource() {
            return null;
        }

        @Override
        public File getTargetDirectory() {
            return null;
        }

        @Override
        public String getTargetPackage() {
            return null;
        }

        @Override
        public char[] getPropertyWordDelimiters() {
            return new char[0];
        }

        @Override
        public boolean isUseLongIntegers() {
            return false;
        }

        @Override
        public boolean isUseDoubleNumbers() {
            return false;
        }

        @Override
        public boolean isUseBigDecimals() {
            return false;
        }

        @Override
        public boolean isIncludeHashcodeAndEquals() {
            return false;
        }

        @Override
        public boolean isIncludeToString() {
            return false;
        }

        @Override
        public AnnotationStyle getAnnotationStyle() {
            return null;
        }

        @Override
        public Class<? extends Annotator> getCustomAnnotator() {
            return null;
        }

        @Override
        public Class<? extends RuleFactory> getCustomRuleFactory() {
            return null;
        }

        @Override
        public boolean isIncludeJsr303Annotations() {
            return false;
        }

        @Override
        public SourceType getSourceType() {
            return null;
        }

        @Override
        public boolean isRemoveOldOutput() {
            return false;
        }

        @Override
        public String getOutputEncoding() {
            return null;
        }

        @Override
        public boolean isUseJodaDates() {
            return false;
        }

        @Override
        public boolean isUseJodaLocalDates() {
            return false;
        }

        @Override
        public boolean isUseJodaLocalTimes() {
            return false;
        }

        @Override
        public boolean isUseCommonsLang3() {
            return false;
        }

        @Override
        public boolean isParcelable() {
            return false;
        }

        @Override
        public FileFilter getFileFilter() {
            return null;
        }

        @Override
        public boolean isInitializeCollections() {
            return false;
        }

        @Override
        public String getClassNamePrefix() {
            return null;
        }

        @Override
        public String getClassNameSuffix() {
            return null;
        }

        @Override
        public boolean isIncludeConstructors() {
            return false;
        }

        @Override
        public boolean isConstructorsRequiredPropertiesOnly() {
            return false;
        }

        @Override
        public boolean isIncludeAdditionalProperties() {
            return false;
        }

        @Override
        public boolean isIncludeAccessors() {
            return false;
        }

        @Override
        public String getTargetVersion() {
            return null;
        }

        @Override
        public boolean isIncludeDynamicAccessors() {
            return false;
        }

        @Override
        public String getDateTimeType() {
            return null;
        }

        @Override
        public String getDateType() {
            return null;
        }

        @Override
        public String getTimeType() {
            return null;
        }
}

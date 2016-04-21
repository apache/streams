package org.apache.streams.plugins;

import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.URLUtil;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sblackmon on 3/27/16.
 */
public class StreamsPojoGenerationConfig extends DefaultGenerationConfig {

    private String sourceDirectory;
    private List<String> sourcePaths;
    private String targetPackage;
    private String targetDirectory;

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setSourcePaths(List<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    public String getTargetPackage() {
        return targetPackage;
    }

    @Override
    public File getTargetDirectory() {
        return new File(targetDirectory);
    }

    @Override
    public Iterator<URL> getSource() {
        if (null != sourceDirectory) {
            return Collections.singleton(URLUtil.parseURL(sourceDirectory)).iterator();
        }
        List<URL> sourceURLs = new ArrayList<URL>();
        if( sourcePaths != null && sourcePaths.size() > 0)
            for (String source : sourcePaths) {
                sourceURLs.add(URLUtil.parseURL(source));
            }
        return sourceURLs.iterator();
    }

}

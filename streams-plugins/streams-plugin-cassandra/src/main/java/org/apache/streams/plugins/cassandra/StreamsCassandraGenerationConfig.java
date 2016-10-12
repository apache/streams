package org.apache.streams.plugins.cassandra;

import org.apache.streams.util.schema.GenerationConfig;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.util.URLUtil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Configures StreamsHiveResourceGenerator
 *
 *
 */
public class StreamsCassandraGenerationConfig extends DefaultGenerationConfig implements GenerationConfig {

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    private String sourceDirectory;
    private List<String> sourcePaths = new ArrayList<String>();
    private String targetDirectory;
    private int maxDepth = 1;

    public Set<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(Set<String> exclusions) {
        this.exclusions = exclusions;
    }

    private Set<String> exclusions = new HashSet<String>();

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setSourcePaths(List<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public File getTargetDirectory() {
        return new File(targetDirectory);
    }

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

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}

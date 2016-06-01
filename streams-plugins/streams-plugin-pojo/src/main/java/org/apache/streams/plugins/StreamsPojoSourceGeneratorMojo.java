package org.apache.streams.plugins;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.maven.Jsonschema2PojoMojo;
import org.jsonschema2pojo.maven.ProjectClasspath;
import org.jsonschema2pojo.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Mojo(  name = "generate-sources",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
@Execute(   goal = "generate-sources",
            phase = LifecyclePhase.GENERATE_SOURCES
)
public class StreamsPojoSourceGeneratorMojo extends AbstractMojo {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGeneratorMojo.class);

    private volatile MojoFailureException mojoFailureException;

    @Component
    public MavenProject project;

    @Parameter( defaultValue = "${project.basedir}", readonly = true )
    public File basedir;

    @Parameter( defaultValue = "./src/main/jsonschema", readonly = true ) // Maven 3 only
    public String sourceDirectory;

    @Parameter( readonly = true ) // Maven 3 only
    public List<String> sourcePaths;

    @Parameter(defaultValue = "./target/generated-sources/pojo", readonly = true)
    public String targetDirectory;

    @Parameter(readonly = true)
    public String targetPackage;

    public void execute() throws MojoExecutionException, MojoFailureException {

        addProjectDependenciesToClasspath();

        StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

        if( sourcePaths != null && sourcePaths.size() > 0)
            config.setSourcePaths(sourcePaths);
        else
            config.setSourceDirectory(sourceDirectory);
        config.setTargetPackage(targetPackage);
        config.setTargetDirectory(targetDirectory);

        StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);
        streamsPojoSourceGenerator.run();

    }

    private void addProjectDependenciesToClasspath() {

        try {

            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newClassLoader = new ProjectClasspath().getClassLoader(project, oldClassLoader, getLog());
            Thread.currentThread().setContextClassLoader(newClassLoader);

        } catch (DependencyResolutionRequiredException e) {
            LOGGER.info("Skipping addition of project artifacts, there appears to be a dependecy resolution problem", e);
        }

    }

}
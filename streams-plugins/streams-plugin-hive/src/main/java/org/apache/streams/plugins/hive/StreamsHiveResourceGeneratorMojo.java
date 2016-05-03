package org.apache.streams.plugins.hive;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

@Mojo(  name = "hive",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
@Execute(   goal = "hive",
            phase = LifecyclePhase.GENERATE_SOURCES
)
public class StreamsHiveResourceGeneratorMojo extends AbstractMojo {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsHiveResourceGeneratorMojo.class);

    @Component
    private MavenProject project;

//    @Component
//    private Settings settings;
//
//    @Parameter( defaultValue = "${localRepository}", readonly = true, required = true )
//    protected ArtifactRepository localRepository;
//
//    @Parameter( defaultValue = "${plugin}", readonly = true ) // Maven 3 only
//    private PluginDescriptor plugin;
//
    @Parameter( defaultValue = "${project.basedir}", readonly = true )
    private File basedir;

    @Parameter( defaultValue = "./src/main/jsonschema", readonly = true ) // Maven 3 only
    public String sourceDirectory;

    @Parameter( readonly = true ) // Maven 3 only
    public List<String> sourcePaths;

    @Parameter(defaultValue = "./target/generated-sources/streams-plugin-hive", readonly = true)
    public String targetDirectory;

    public void execute() throws MojoExecutionException {

        //addProjectDependenciesToClasspath();

        StreamsHiveGenerationConfig config = new StreamsHiveGenerationConfig();

        if( sourcePaths != null && sourcePaths.size() > 0)
            config.setSourcePaths(sourcePaths);
        else
            config.setSourceDirectory(sourceDirectory);
        config.setTargetDirectory(targetDirectory);

        StreamsHiveResourceGenerator streamsPojoScala = new StreamsHiveResourceGenerator(config);
        Thread thread = new Thread(streamsPojoScala);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return;
    }

}
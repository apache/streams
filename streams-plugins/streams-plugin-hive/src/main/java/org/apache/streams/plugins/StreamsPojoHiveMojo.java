package org.apache.streams.plugins;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Mojo(  name = "hive",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
@Execute(   goal = "hive",
            phase = LifecyclePhase.GENERATE_SOURCES
)
public class StreamsPojoHiveMojo extends AbstractMojo {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoHiveMojo.class);

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

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File target;

    @Parameter(defaultValue = "org.apache.streams.pojo.json", readonly = true)
    private String[] packages;

    public void execute() throws MojoExecutionException {
        StreamsPojoHive streamsPojoScala = new StreamsPojoHive(this);
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

    public File getTarget() {
        return target;
    }

    public String[] getPackages() {
        return packages;
    }
}
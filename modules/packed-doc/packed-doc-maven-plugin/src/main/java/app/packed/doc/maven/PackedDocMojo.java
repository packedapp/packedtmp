package app.packed.doc.maven;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.Assembly;
import app.packed.doc.generator.PackedDocGenerator;

/**
 * Maven plugin for generating Docusaurus documentation from Packed applications.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackedDocMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Fully qualified class name of the Assembly to document.
     */
    @Parameter(property = "packed.assemblyClass", required = true)
    private String assemblyClass;

    /**
     * Output directory for generated documentation.
     */
    @Parameter(property = "packed.outputDirectory", defaultValue = "${project.build.directory}/packed-doc")
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Generating PackedDoc for assembly: " + assemblyClass);
        getLog().info("Output directory: " + outputDirectory);

        try {
            // Build classpath from project dependencies
            List<String> classpathElements = project.getCompileClasspathElements();
            URL[] urls = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); i++) {
                urls[i] = new File(classpathElements.get(i)).toURI().toURL();
            }

            // Create classloader with project dependencies
            try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
                // Load assembly class
                Class<?> clazz = classLoader.loadClass(assemblyClass);
                if (!Assembly.class.isAssignableFrom(clazz)) {
                    throw new MojoExecutionException(assemblyClass + " is not an Assembly");
                }

                // Create assembly instance
                @SuppressWarnings("unchecked")
                Assembly assembly = ((Class<? extends Assembly>) clazz).getDeclaredConstructor().newInstance();

                // Get application mirror
                ApplicationMirror mirror = App.mirrorOf(assembly);

                // Generate documentation
                Path outputPath = outputDirectory.toPath();
                PackedDocGenerator generator = new PackedDocGenerator(outputPath);
                generator.generate(mirror);

                getLog().info("Documentation generated successfully");
            }

        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate documentation", e);
        }
    }
}

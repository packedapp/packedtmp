package app.packed.doc.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.Assembly;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;

/**
 * Documentation generator for Packed applications.
 * Generates static HTML sites from ApplicationMirror using JTE and Pico.css.
 */
public class PackedDocGenerator {

    private final Path outputDirectory;
    private final TemplateEngine templateEngine;

    public PackedDocGenerator(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        CodeResolver codeResolver = new ResourceCodeResolver("templates");
        this.templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
    }

    public void generate(ApplicationMirror mirror) throws IOException {
        String appName = mirror.name();

        // Create output directories
        Path cssDir = outputDirectory.resolve("css");
        Files.createDirectories(cssDir);

        // Copy pico.css to output
        copyResource("pico.min.css", cssDir.resolve("pico.min.css"));

        // Generate index.html using JTE
        generateIndexHtml(appName);
    }

    private void copyResource(String resourceName, Path destination) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            Files.copy(is, destination);
        }
    }

    private void generateIndexHtml(String appName) throws IOException {
        StringOutput output = new StringOutput();
        templateEngine.render("index.jte", appName, output);
        Files.writeString(outputDirectory.resolve("index.html"), output.toString());
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: PackedDocGenerator <assemblyClassName> [outputDirectory]");
            System.exit(1);
        }

        String assemblyClassName = args[0];
        Path outputDir = args.length > 1 ? Path.of(args[1]) : Path.of("target/packed-doc");

        try {
            // Load assembly class
            Class<?> clazz = Class.forName(assemblyClassName);
            if (!Assembly.class.isAssignableFrom(clazz)) {
                System.err.println("Error: " + assemblyClassName + " is not an Assembly");
                System.exit(1);
            }

            // Create assembly instance
            @SuppressWarnings("unchecked")
            Assembly assembly = ((Class<? extends Assembly>) clazz).getDeclaredConstructor().newInstance();

            // Get application mirror
            ApplicationMirror mirror = App.mirrorOf(assembly);

            // Generate documentation
            PackedDocGenerator generator = new PackedDocGenerator(outputDir);
            generator.generate(mirror);

            System.out.println("Documentation generated at: " + outputDir.toAbsolutePath());

        } catch (ClassNotFoundException e) {
            System.err.println("Error: Class not found: " + assemblyClassName);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error generating documentation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

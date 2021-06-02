package app.packed.application.host;

import app.packed.extension.Extension;

// Is this an extension???? Or a runtime thingy...
// Tror det at det er, er vi gerne vil have en let maade at depl
public class ApplicationDeploymentExtension extends Extension {

    static {
        $dependsOn(ApplicationHostExtension.class);
        // $dependsOn(ClassLoaderExtension?.class);
        // $dependsOn(FileExtension.class);
    }

    ApplicationDeploymentExtension() {}

    // Maaske bruger vi FileExtension...

}

// OpenWorldExtension

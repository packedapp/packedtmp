package app.packed.job;

import app.packed.application.ApplicationRuntimeExtension;
import app.packed.extension.Extension;

public class JobExtension extends Extension {
    JobExtension() {}

    // Set result type
    // parseAssembly()
    
    static {
        $dependsOn(ApplicationRuntimeExtension.class);
    }
}

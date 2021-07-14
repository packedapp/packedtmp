package app.packed.application.deployer;

import app.packed.application.host.ApplicationHostExtension;
import app.packed.extension.Extension;

public class ApplicationDeployedExtension extends Extension {

    static {
        $dependsOn(ApplicationHostExtension.class);
    }
}

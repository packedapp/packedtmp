package app.packed.application.deployer;

import app.packed.application.host.ApplicationExtension;
import app.packed.extension.Extension;

public class ApplicationDeployedExtension extends Extension {

    static {
        $dependsOnAlways(ApplicationExtension.class);
    }
}

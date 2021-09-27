package app.packed.application.deployer;

import app.packed.bundle.host.ApplicationHostExtension;
import app.packed.extension.Extension;

public class ApplicationDeployerExtension extends Extension {

    static {
        $dependsOn(ApplicationHostExtension.class);
    }
}

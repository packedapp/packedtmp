package app.packed.file;

import app.packed.application.ApplicationAssembly;
import app.packed.application.ApplicationDriver;

abstract class Plugin extends ApplicationAssembly<Void> {

    /** The plugin container driver. Disables all file system access. */
    private static final ApplicationDriver<Void> DRIVER = ApplicationDriver.builder().disableExtension(FileExtension.class).buildInstanceless(Void.class)
            .with(FileWirelets.disableWrite());

    protected Plugin() {
        super(DRIVER);
    }

    protected Plugin(ApplicationDriver<Void> driver) {
        super(driver.with(FileWirelets.disableWrite()));
    }
}

class ZooPlugin extends Plugin {

    @Override
    protected void build() {
        provideInstance("qweqwe");
    }
}

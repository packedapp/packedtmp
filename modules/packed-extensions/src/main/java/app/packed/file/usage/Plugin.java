package app.packed.file.usage;

import app.packed.bundle.BaseBundle;

abstract class Plugin extends BaseBundle {

    /** The plugin container driver. Disables all file system access. */
//    static final ApplicationDriver<Void> DRIVER = ApplicationDriver.builder().disableExtension(FileExtension.class).buildInstanceless(Void.class)
//            .with(FileWirelets.disableWrite());

//    protected Plugin() {
//        super(DRIVER);
//    }
//
//    protected Plugin(ApplicationDriver<Void> driver) {
//        super(driver.with(FileWirelets.disableWrite()));
//    }
}

class ZooPlugin extends Plugin {

    @Override
    protected void build() {
        provideInstance("qweqwe");
    }
}

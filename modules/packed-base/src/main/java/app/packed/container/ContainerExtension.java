package app.packed.container;

import app.packed.component.Wirelet;
import app.packed.extension.Extension;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

public class ContainerExtension extends Extension {
    
    /** The service manager. */
    final ContainerSetup container;

    final ExtensionSetup extension;

    /**
     * Create a new container extension.
     * 
     * @param setup
     *            an extension setup object (hidden).
     */
    /* package-private */ ContainerExtension(ExtensionSetup extension) {
        this.extension = extension;
        this.container = extension.container;
    }

    public ContainerMirror link(Assembly<?> assembly, Wirelet... wirelets) {
        return container.link(assembly, container.realm, wirelets);
    }

    public class Sub {

        public ContainerMirror link(Assembly<?> assembly, Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }
    }
}

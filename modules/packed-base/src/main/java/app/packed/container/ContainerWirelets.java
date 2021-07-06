package app.packed.container;

import app.packed.component.Wirelet;
import app.packed.extension.Extension;

/**
 * Wirelets that can be specified when wiring a container.
 */
public final class ContainerWirelets {
    private ContainerWirelets() {}

    /**
     * Returns a wirelet that will disable the specified extensions. Inherited
     * 
     * @param extensionTypes
     *            the type of extensions to disable
     * @return the wirelet
     */
    // Skal vi have en specific ExtensionDisabledException???
    
    @SafeVarargs
    public static Wirelet disableExtension(Class<? extends Extension>... extensionTypes) {
        throw new UnsupportedOperationException();
    }
    
    // peekExtensionInstall(ExtensionMirror?)
    // There are some possibilities...

    // disableExtension(Predicate<?>, Class<?>... classes)

    // disableExtensionForUser
    // disableExtensionForExtension
}

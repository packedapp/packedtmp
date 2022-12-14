package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;
import internal.app.packed.container.ContainerSetup;

/**
 * A container handle is a reference to an installed container, private to the extension that installed the container.
 */

// Vi skal vel have de samme genererings metoder som OperationSetup
// Taenker ikke man skal have adgang direkte til handled

public final class ContainerHandle {

    /** The container that can be configured. */
    final ContainerSetup container;

    ContainerHandle(ContainerSetup container) {
        this.container = requireNonNull(container);
    }

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    public Set<Class<? extends Extension<?>>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @throws IllegalStateException
     *             if the container is no longer configurable
     */
    protected void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("This container is no longer configurable");
        }
    }

    /**
     * Returns whether or not the bean is still configurable.
     * 
     * @return {@code true} if the bean is still configurable
     */
    public boolean isConfigurable() {
        return !container.assembly.isClosed();
    }

    // Hmm, skal vi have selve handles'ene?
    // Jo det skal vi faktisk nok...

    /**
     * If the bean is registered with its own lifetime. This method returns a list of the lifetime operations of the bean.
     * <p>
     * The operations in the returned list must be computed exactly once. For example, via
     * {@link OperationHandle#generateMethodHandle()}. Otherwise a build exception will be thrown. Maybe this goes for all
     * operation customizers.
     * 
     * @return a list of lifetime operations
     * 
     */
    public List<OperationHandle> lifetimeOperations() {
        return List.of();
    }
   
    public void named(String name) {
        container.named(name);
    }
    


    public void setErrorHandler(ErrorHandler errorHandler) {

    }

    public void specializeMirror(Supplier<? extends ContainerMirror> supplier) {

    }

}

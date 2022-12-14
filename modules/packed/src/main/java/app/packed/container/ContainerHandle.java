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
    public void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("This container is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ContainerHandle h && container == h.container;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return container.hashCode();
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
     * If the container is registered with its own lifetime. This method returns a list of the container's lifetime
     * operations.
     * 
     * @return a list of lifetime operations if the container has its own lifetime
     */
    public List<OperationHandle> lifetimeOperations() {
        return List.of();
    }

    public void named(String name) {
        container.named(name);
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        checkIsConfigurable();
    }

    // Hvis vi linker skal vi jo saette det inde...
    public void specializeMirror(Supplier<? extends ContainerMirror> supplier) {
        checkIsConfigurable();
    }
}

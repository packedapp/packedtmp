package app.packed.container;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;

/**
 * A container handle is a reference to an installed container, private to the extension that installed the container.
 */
// Vi skal vel have de samme genererings metoder som OperationSetup
// Taenker ikke man skal have adgang direkte til handled
public interface ContainerHandle {

    ContainerHandle addContainer(Wirelet... wirelets);
    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     *
     * @return a set of disabled extensions
     */
    Set<Class<? extends Extension<?>>> bannedExtensions();

    /**
     *
     * @throws IllegalStateException
     *             if the container is no longer configurable
     */
    void checkIsConfigurable();

    /**
     * Returns whether or not the bean is still configurable.
     * <p>
     * If an assembly was used to create the container. The handle is never configurable.
     *
     * @return {@code true} if the bean is still configurable
     */
    boolean isConfigurable();

    // Hmm, skal vi have selve handles'ene?
    // Jo det skal vi faktisk nok...

    /**
     * If the container is registered with its own lifetime. This method returns a list of the container's lifetime
     * operations.
     *
     * @return a list of lifetime operations if the container has its own lifetime
     */
    List<OperationHandle> lifetimeOperations();

    void named(String name);

    void setErrorHandler(ErrorHandler errorHandler);

    // Hvis vi linker skal vi jo saette det inde...
    void specializeMirror(Supplier<? extends ContainerMirror> supplier);
}

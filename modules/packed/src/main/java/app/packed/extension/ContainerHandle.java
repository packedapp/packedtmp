package app.packed.extension;

import java.util.List;
import java.util.Set;

import app.packed.errorhandling.ErrorHandler;

/**
 * A container handle is a reference to an installed container, private to the extension that installed the container.
 */
public interface ContainerHandle {

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
    default void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("This container is no longer configurable");
        }
    }

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

    /**
     * Sets the name of the container.
     *
     * @param name
     *
     * @throws IllegalStateException
     *             if the container is no long configurable
     *
     * @see ContainerConfiguration#named(String)
     * @see ContainerInstaller#named(String)
     */
    void named(String name);

    /**
     * @param errorHandler
     *
     * @see ContainerInstaller#errorHandle(ErrorHandler)
     *
     * @throws IllegalStateException
     *             if the container is no long configurable
     */
    // Det er udelukkende hvis fx usereren skal have lov at besteem error handleren
    void setErrorHandler(ErrorHandler errorHandler);
}

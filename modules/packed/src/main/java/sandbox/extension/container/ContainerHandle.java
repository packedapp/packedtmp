package sandbox.extension.container;

import java.util.List;
import java.util.Set;

import app.packed.application.OldApplicationPath;
import app.packed.extension.Extension;
import internal.app.packed.container.PackedContainerHandle;
import sandbox.extension.operation.OperationHandle;

/**
 * A container handle is created when a container is installed an.
 *
 * a reference to an installed container, private to the extension that installed the container.
 *
 * <p>
 * A lot of methods on this class is also available on {@link ContainerBuilder}.
 */
public sealed interface ContainerHandle permits PackedContainerHandle {

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
     * {@return an unmodifiable view of the extensions that are currently used by this container.}
     *
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    Set<Class<? extends Extension<?>>> extensionTypes();

    /**
     * Returns whether or not the bean is still configurable.
     * <p>
     * If an assembly was used to create the container. The handle is never configurable.
     *
     * @return {@code true} if the bean is still configurable
     */
    boolean isConfigurable();

    /**
     * Returns whether or not the specified extension is used by this extension, other extensions, or user code in the same
     * container as this extension.
     *
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is currently in use, otherwise {@code false}
     * @implNote Packed does not perform detailed tracking on which extensions use other extensions. As a consequence it
     *           cannot give a more detailed answer about who is using a particular extension
     */
    boolean isExtensionUsed(Class<? extends Extension<?>> extensionType);

    /**
     * If the container is registered with its own lifetime. This method returns a list of the container's lifetime
     * operations.
     *
     * @return a list of lifetime operations if the container has its own lifetime
     */
    List<OperationHandle> lifetimeOperations();

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #named(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     *
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     *
     * @return the path of this configuration.
     */
    OldApplicationPath path();
}

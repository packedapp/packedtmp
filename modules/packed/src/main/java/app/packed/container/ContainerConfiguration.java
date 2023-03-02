package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.application.ApplicationPath;
import app.packed.extension.Extension;
import app.packed.extension.container.ContainerHandle;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.Nullable;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedContainerHandle;

/**
 * The configuration of a container.
 * <p>
 * Unlike {@link app.packed.bean.BeanConfiguration} this class cannot be extended.
 */
public final class ContainerConfiguration {

    /**
     * A marker configuration object indicating that an assembly (or composer) has already been used for building a
     * container. Should never be exposed to end-users.
     */
    static final ContainerConfiguration USED = new ContainerConfiguration();

    /** The container we are configuring. Is only null for {@link #USED}. */
    @Nullable
    final PackedContainerHandle handle;

    /** Used by {@link #USED}. */
    private ContainerConfiguration() {
        this.handle = null;
    }

    /**
     * Create a new container configuration.
     *
     * @param handle
     *            the container handle
     */
    public ContainerConfiguration(ContainerHandle handle) {
        this.handle = (PackedContainerHandle) requireNonNull(handle, "handle is null");
    }

    /**
     * Checks that the container's assembly is still configurable.
     *
     * @throws IllegalStateException
     *             if the container's assembly is no longer configurable
     */
    // TODO: not currently used
    protected void checkIsConfigurable() {
        if (!handle.container().assembly.isConfigurable()) {
            throw new IllegalStateException("This container is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ContainerConfiguration bc && handle.equals(bc.handle);
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used by this container in no particular order.}
     *
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return handle.extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return handle.hashCode();
    }

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
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return handle.isExtensionUsed(extensionType);
    }

    public LifetimeKind lifetimeKind() {
        return handle.container().lifetime.lifetimeKind();
    }

    /**
     * Sets the name of the component. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * If no name is explicitly set on a component. A name will be assigned to the component (at build time) in such a way
     * that it will have a unique name among other sibling components.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see Wirelet#named(String)
     */
    public ContainerConfiguration named(String name) {
        handle.named(name);
        return this;
    }

    /**
     * Registers a callback that will be invoked whenever
     * <p>
     * If {@code Extension.class} is specified. The given action is invoked for every extension that is used.
     *
     * @param <E>
     *            the type of
     * @param extensionType
     * @param action
     *            the action to invoke
     */
    // Skal vi have en version der tager en [Runnable alternative] hvis den ikke bliver installeret?
    public <E extends Extension<E>> void onFirstUse(Class<E> extensionType, Consumer<? super E> action) {
        // bruger assignable
        // onFirstUse(Extension.class, ()-> Extension isUsed);
        // Taenker jeg kunne vaere brugbart i rooten....
        /// Fx for at sige (SRExtension.class, ()-> e.setPrivatelyOwned());
        // Er maaske taenkt
        throw new UnsupportedOperationException();
    }

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
    public ApplicationPath path() {
        return handle.path();
    }

    // never selects extension wirelets...
    public <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return handle.container().selectWirelets(wireletClass);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return handle.toString();
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is the first time an extension of the specified type has been requested. This method will create a new
     * instance of the extension. This instance will then be returned for all subsequent requests for the same extension
     * type.
     *
     * @param <E>
     *            the type of extension to return
     * @param extensionClass
     *            the Class object corresponding to the extension type
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the underlying container is no longer configurable and the specified type of extension has not been
     *             used previously
     * @see #extensionsTypes()
     * @see BaseAssembly#use(Class)
     */
    public <E extends Extension<?>> E use(Class<E> extensionClass) {
        ExtensionSetup extension = handle.container().useExtension(extensionClass, null);
        return extensionClass.cast(extension.instance());
    }
}

package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.bean.BeanConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerLocal.LocalAccessor;
import app.packed.extension.Extension;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.Nullable;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.util.types.ClassUtil;
import sandbox.extension.container.ContainerHandle;

/**
 * The configuration of a container.
 * <p>
 * Unlike {@link app.packed.bean.BeanConfiguration} this class cannot be extended.
 */
// Could let it be extendable. But it would only be usable through methods on extensions. Although
// An assembly could return an instance of it
public non-sealed class ContainerConfiguration extends ComponentConfiguration implements LocalAccessor {

    /**
     * A marker configuration object indicating that an assembly (or composer) has already been used for building a
     * container. Should never be exposed to end-users.
     */
    static final ContainerConfiguration USED = new ContainerConfiguration();

    /** The container we are configuring. Is only null for {@link #USED}. */
    @Nullable
    final ContainerSetup container;

    /** Used by {@link #USED}. */
    private ContainerConfiguration() {
        this.container = null;
    }

    /**
     * Create a new container configuration.
     *
     * @param handle
     *            the container handle
     */
    public ContainerConfiguration(ContainerHandle handle) {
        this.container = (ContainerSetup) requireNonNull(handle, "handle is null");
        container.initConfiguration(this);
    }

    public final Stream<BeanConfiguration> beans() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks that the container's assembly is still configurable.
     *
     * @throws IllegalStateException
     *             if the container's assembly is no longer configurable
     */
    protected void checkIsConfigurable() {
        container.checkIsConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return container.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration componentTag(String... tags) {
        return container.componentTag(tags);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ContainerConfiguration bc && container.equals(bc.container);
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used by this container in no particular order.}
     *
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container.extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return container.hashCode();
    }

    public boolean isAssemblyRoot() {
        return container.isAssemblyRoot();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return container.isConfigurable();
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
        return container.isExtensionUsed(extensionType);
    }

    public LifetimeKind lifetimeKind() {
        return container.lifetime.lifetimeKind();
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
        container.named(name);
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
    <E extends Extension<E>> void onFirstUse(Class<E> extensionType, Consumer<? super E> action) {
        // bruger assignable
        // onFirstUse(Extension.class, ()-> Extension isUsed);
        // Taenker jeg kunne vaere brugbart i rooten....
        /// Fx for at sige (SRExtension.class, ()-> e.setPrivatelyOwned());
        // Er maaske taenkt
        throw new UnsupportedOperationException();
    }

    /**
     * @param <W>
     *            the type of wirelet to select
     * @param wireletClass
     *            the type of wirelet to select
     * @return A wirelet selection
     */
    public <W extends ApplicationWirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        ClassUtil.checkProperSubclass(ApplicationWirelet.class, wireletClass, "wireletClass");
        return container.selectWireletsUnsafe(wireletClass);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return container.toString();
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
        ExtensionSetup extension = container.useExtension(extensionClass, null);
        return extensionClass.cast(extension.instance());
    }
}

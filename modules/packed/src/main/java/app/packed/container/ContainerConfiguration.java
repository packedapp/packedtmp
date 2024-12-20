package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.bean.BeanConfiguration;
import app.packed.build.action.BuildActionable;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;
import app.packed.util.Nullable;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.util.types.ClassUtil;

/**
 * The configuration of a container.
 * <p>
 * Unlike {@link app.packed.bean.BeanConfiguration} this class cannot be extended.
 */
public non-sealed class ContainerConfiguration extends ComponentConfiguration implements ContainerBuildLocal.Accessor {

    /** The container we are configuring. */
    @Nullable
    private final ContainerHandle<?> handle;

    public ContainerConfiguration(ContainerHandle<?> handle) {
        this.handle = requireNonNull(handle);
    }

    /**
     * {@return a stream of all the beans installed in this container}
     * <p>
     * The returned stream does not include beans owned by extensions.
     *
     * @see ContainerMirror#beans()
     */
    // We install using base(), but have beans here...
    public final Stream<? extends BeanConfiguration> beans() {
        return handle.container.beans.stream().filter(b -> b.owner().isApplication()).map(b -> b.handle().configuration()).filter(c -> c != null);
    }

    @SuppressWarnings("unchecked")
    public final <T extends BeanConfiguration> Stream<T> beans(Class<? extends BeanConfiguration> beanClass) {
        return (Stream<T>) beans().filter(beanClass::isInstance);
    }


    /** {@inheritDoc} */
    @Override
    @BuildActionable("container.addTags")
    public ComponentConfiguration tag(String... tags) {
        checkIsConfigurable();
        handle.componentTag(tags);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<String> tags() {
        return handle.componentTags();
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used by this container in no particular order.}
     *
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    public final Set<Class<? extends Extension<?>>> extensionTypes() {
        return handle.container.extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    protected final ContainerHandle<?> handle() {
        return handle;
    }

    public final boolean isApplicationRoot() {
        return handle.container.isApplicationRoot();
    }

    public final boolean isAssemblyRoot() {
        return handle.container.isAssemblyRoot();
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
    public final boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return handle.container.isExtensionUsed(extensionType);
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
    @BuildActionable("container.named")
    public ContainerConfiguration named(String name) {
        checkIsConfigurable();
        handle.container.named(name);
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
    public final <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        ClassUtil.checkProperSubclass(Wirelet.class, wireletClass, "wireletClass");
        return handle.container.selectWireletsUnsafe(wireletClass);
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
    @BuildActionable("container.installExtension")
    public final <E extends Extension<?>> E use(Class<E> extensionClass) {
        ExtensionSetup extension = handle.container.useExtension(extensionClass, null);
        return extensionClass.cast(extension.instance());
    }
}

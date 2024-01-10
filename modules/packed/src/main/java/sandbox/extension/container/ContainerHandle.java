package sandbox.extension.container;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.component.Component;
import app.packed.container.Assembly;
import app.packed.container.ContainerLocal;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.Extension;
import app.packed.util.Key;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.NonRootContainerBuilder;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.extension.context.ContextSpanKind;
import sandbox.extension.operation.OperationHandle;

/**
 * A container handle is created when a container is installed an.
 *
 * a reference to an installed container, private to the extension that installed the container.
 *
 * <p>
 * A lot of methods on this class is also available on {@link ContainerBuilder}.
 */
public sealed interface ContainerHandle extends Component , ContainerLocal.LocalAccessor permits ContainerSetup {

    /**
     * Checks that the container is still configurable, or throws an exception.
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
     * Returns whether or not the container is still configurable.
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
     * @implNote The framework does not perform detailed tracking on which extensions use other extensions. As a consequence
     *           it cannot give a more detailed answer about who is using a particular extension
     * @see ContainerConfiguration#isExtensionUsed(Class)
     * @see ContainerMirror#isExtensionUsed(Class)
     */
    boolean isExtensionUsed(Class<? extends Extension<?>> extensionType);

    /**
     * This method returns a list of the container's lifetime operations.
     * <p>
     * If the lifetime of the container container cannot be explicitly controlled, for example, if it is a child container.
     * The returned list is empty.
     *
     * @return a list of lifetime operations of this container.
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

    /**
     * A builder for a container (handle).
     *
     * @see BaseExtensionPoint#addCodeGenerated(BeanConfiguration, Class, Supplier)
     * @see BaseExtensionPoint#beanInstallerForExtension(app.packed.extension.bean.BeanTemplate,
     *      app.packed.extension.ExtensionPoint.UseSite)
     */
    public sealed interface Builder permits NonRootContainerBuilder {

        /**
         * Creates a new container using the specified assembly.
         * <p>
         * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}.
         * Configuration of the new container must be done prior to calling this method.
         *
         * @param assembly
         *            the assembly to link
         * @param wirelets
         *            optional wirelets
         * @return a container handle representing the new container
         *
         * @see #build(Wirelet...)
         */
        ContainerHandle build(Assembly assembly, Wirelet... wirelets);

        /**
         * Creates a new configurable container.
         *
         * @param wirelets
         *            optional wirelets
         * @return a container handle representing the new container
         *
         * @see #install(Assembly, Wirelet...)
         */
        ContainerHandle build(Wirelet... wirelets);

        /**
         * Creates the new container and adds this extension to the new container.
         * <p>
         * The extension in new the container can be obtained by calling {@link Extension#fromHandle(ContainerHandle)}
         *
         * @return a container handle representing the new container
         *
         * @see app.packed.extension.Extension#fromHandle(ContainerHandle)
         */
        ContainerHandle buildAndUseThisExtension(Wirelet... wirelets);

        // Only Managed-Operation does not require a wrapper
        // For now this method is here. Might move it to the actual CHC at some point

        // Hmm, don't know if need a carrier instance, if we have implicit construction
//        /**
//         * @return
//         * @throws UnsupportedOperationException
//         *             if a carrier type was not defined in the container template
//         */
//        default ContainerCarrierConfiguration<?> carrierInstance() {
//            throw new UnsupportedOperationException();
//        }

        /**
         * Provides constants per Carrier Instance for this particular container builder
         *
         * @param <T>
         * @param key
         * @param arg
         * @return
         *
         * @see ExtensionLink#ofConstant(Class, Object)
         */
        default <T> Builder carrierProvideConstant(Class<T> key, T constant) {
            return carrierProvideConstant(Key.of(key), constant);
        }

        /**
         * @see FromLifetimeChannel
         */
        <T> Builder carrierProvideConstant(Key<T> key, T constant);

        /**
         *
         * @param holderConfiguration
         * @return
         * @see app.packed.extension.BaseExtensionPoint#installContainerHolder(Class)
         * @throws IllegalArgumentException
         *             if the holder class of the bean does not match the holder type set when creating the container template.
         */
        // LifetimeCarrier?
        default Builder carrierUse(ContainerCarrierBeanConfiguration<?> holderConfiguration) {
            // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum
            // supportere det
            // Hvis vi vil dele den...

            // Det betyder ogsaa vi skal lave en wrapper bean alene
            return this;
        }

        /**
         * <p>
         * TODO: How do we handle conflicts? I don't think we should fail
         * <p>
         * TODO This is probably overridable by Wirelet.named()
         * <p>
         * Beans not-capitalized? Containers capitalized
         *
         * @param name
         *            the name of the container
         * @return this builder
         */
        Builder named(String name);

        /**
         * Sets the value of the specified container local for the container being built.
         *
         * @param <T>
         *            the type of value the container local holds
         * @param local
         *            the container local to set
         * @param value
         *            the value of the local
         * @return this builder
         */
        // Do we allow non-container scope??? I don't think so
        <T> Builder localSet(ContainerLocal<T> containerLocal, T value);

        /**
         * Sets a supplier that creates a special container mirror instead of the generic {@code ContainerMirror} when
         * requested.
         *
         * @param supplier
         *            the supplier used to create the container mirror
         * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
         *          must be returned
         */
        Builder specializeMirror(Supplier<? extends ContainerMirror> supplier);

        // The application will fail to build if the installing extension
        // is not used by. Is only applicable for new(Assembly)
        // Maaske er det fint bare en wirelet der kan tage en custom besked?
        default Builder zBuildAndRequiresThisExtension(Assembly assembly, Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        // ditch beanBlass, and just make sure there is a bean that can do it
        default Builder zContextFromBean(Class<?> beanClass, ContextTemplate template, @SuppressWarnings("exports") ContextSpanKind span) {
            throw new UnsupportedOperationException();
        }

        /**
         * <p>
         * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}
         *
         * @param assembly
         *            the assembly to link
         * @param wirelets
         *            optional wirelets
         * @return a container handle representing the linked container
         */
        default Builder zErrorHandle(ErrorHandler h) {
            return this;
        }

        default Builder zRequireUseOfExtension(String errorMessage) {
            throw new UnsupportedOperationException();
        }
    }
}

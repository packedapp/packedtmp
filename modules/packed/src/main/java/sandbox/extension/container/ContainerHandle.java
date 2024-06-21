package sandbox.extension.container;

import java.util.List;
import java.util.Set;

import app.packed.component.ComponentHandle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLocal;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.Extension;
import internal.app.packed.container.PackedContainerHandle;
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
public sealed interface ContainerHandle<C extends ContainerConfiguration> extends ComponentHandle , ContainerLocal.ContainerLocalAccessor
        permits PackedContainerHandle {

    /** {@return the configuration of the container} */
    C configuration();

    /**
     * {@return an unmodifiable view of the extensions that are currently used by this container.}
     *
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    Set<Class<? extends Extension<?>>> extensionTypes();

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

    interface Zandbox {
        // ditch beanBlass, and just make sure there is a bean that can do it
        default Zandbox zContextFromBean(Class<?> beanClass, ContextTemplate template, @SuppressWarnings("exports") ContextSpanKind span) {
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
        default Zandbox zErrorHandle(ErrorHandler h) {
            return this;
        }
    }

}

// The application will fail to build if the installing extension
// is not used by. Is only applicable for new(Assembly)
// Maaske er det fint bare en wirelet der kan tage en custom besked?
// Smider den paa templaten
//default Zandbox zBuildAndRequiresThisExtension(Assembly assembly, Wirelet... wirelets) {
//    throw new UnsupportedOperationException();
//}


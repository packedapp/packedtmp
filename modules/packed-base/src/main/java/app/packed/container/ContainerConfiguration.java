package app.packed.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Set;

import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * The configuration of a container.
 * <p>
 * Currently only a single concrete implementation exists.
 * 
 * but users are free to create other implementations that restrict the functionality of the default container
 * configuration by overridding this class.
 */

// Ved sgu ikke om man skal kunne override den...Det tror jeg faktisk ikke...
// Eller man maa gerne kunne overskrive den, men taenker ikke Assembly kan tage andet end ContainerConfiguration...
public non-sealed class ContainerConfiguration extends ComponentConfiguration {

    /** A handle that can access superclass private ComponentConfiguration#component(). */
    private static final MethodHandle MH_COMPONENT_CONFIGURATION_COMPONENT = MethodHandles.explicitCastArguments(
            LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class, "component", ComponentSetup.class),
            MethodType.methodType(ContainerSetup.class, ContainerConfiguration.class));

    /** {@return the container setup instance that we are wrapping.} */
    private ContainerSetup container() {
        try {
            return (ContainerSetup) MH_COMPONENT_CONFIGURATION_COMPONENT.invokeExact(this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used}
     * 
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    public Set<Class<? extends Extension>> extensionsTypes() {
        return container().extensionsTypes();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerMirror mirror() {
        return container().mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerConfiguration named(String name) {
        super.named(name);
        return this;
    }

    public <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        return container().selectWirelets(wireletClass);
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is the first time an extension of the specified type has been requested. This method will create a new
     * instance of the extension. This instance will then be returned for all subsequent calls to this method for the same
     * extension type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the underlying container is no longer configurable and an extension of the specified type is not
     *             already in used
     * @see #extensionsTypes()
     */
    public <T extends Extension> T use(Class<T> extensionType) {
        return container().useExtension(extensionType);
    }
}

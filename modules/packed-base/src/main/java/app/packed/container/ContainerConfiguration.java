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
 * <p>
 * Currently only a single concrete implementation exists.
 * 
 * but users are free to create other implementations that restrict the functionality of the default container
 * configuration by overridding this class.
 */
public abstract /* non-sealed */ class ContainerConfiguration extends ComponentConfiguration {

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
     * Returns an unmodifiable view of the extensions that are currently used.
     * 
     * @return an unmodifiable view of the extensions that are currently used
     * 
     * @see #use(Class)
     * @see CommonContainerAssembly#extensions()
     * @see ContainerMirror#extensions()
     */
    protected Set<Class<? extends Extension>> extensions() {
        return container().extensions();
    }

    /** {@inheritDoc} */
    @Override
    protected ContainerMirror mirror() {
        return container().mirror();
    }

    /**
     * Returns an extension of the specified type. If this is the first time an extension of the specified type has been
     * requested. This method will create a new instance of the extension. This instance will be returned for all subsequent
     * calls to this method with the same extension type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if this configuration is no longer configurable and an extension of the specified type has not already
     *             been installed
     * @see #extensions()
     */
    protected <T extends Extension> T use(Class<T> extensionType) {
        return container().useExtension(extensionType);
    }
}

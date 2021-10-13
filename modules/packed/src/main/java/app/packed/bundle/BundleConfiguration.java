package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Set;

import app.packed.application.ApplicationDescriptor;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;
import packed.internal.bundle.BundleSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * The configuration of a container.
 * <p>
 * Currently only a single concrete implementation exists.
 * 
 * but users are free to create other implementations that restrict the functionality of the default container
 * configuration by overriding this class.
 */
public final class BundleConfiguration extends ComponentConfiguration {

    /** A method handle that can access superclass ComponentConfiguration#component(). */
    private static final MethodHandle MH_COMPONENT_CONFIGURATION_COMPONENT = MethodHandles.explicitCastArguments(
            LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class, "component", ComponentSetup.class),
            MethodType.methodType(BundleSetup.class, BundleConfiguration.class));

    /** {@return a descriptor for the application the container is a part of.} */
    public ApplicationDescriptor application() {
        return container().application.descriptor;
    }

    /** {@return the wrapped configuration instance.} */
    BundleSetup container() {
        try {
            return (BundleSetup) MH_COMPONENT_CONFIGURATION_COMPONENT.invokeExact(this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used.}
     * 
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see BundleMirror#extensionsTypes()
     */
    public Set<Class<? extends Extension>> extensionsTypes() {
        return container().extensionsTypes();
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
    public boolean isExtensionUsed(Class<? extends Extension> extensionType) {
        return container().isExtensionUsed(extensionType);
    }

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     */
    public void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        container().realm.setLookup(lookup);
    }

    /** {@return a mirror for the bundle.} */
    @Override
    public BundleMirror mirror() {
        return container().mirror();
    }

    /** {@inheritDoc} */
    @Override
    public BundleConfiguration named(String name) {
        super.named(name);
        return this;
    }

    // Why not just configure the assembly???
    public <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return container().selectWirelets(wireletClass);
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is the first time an extension of the specified type has been requested. This method will create a new
     * instance of the extension. This instance will then be returned for all subsequent calls to this method for the same
     * extension type.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the underlying container is no longer configurable and an extension of the specified type is not
     *             already in used
     * @see #extensionsTypes()
     */
    public <E extends Extension> E use(Class<E> extensionType) {
        return container().useExtension(extensionType);
    }
}

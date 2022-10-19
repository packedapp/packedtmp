package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import internal.app.packed.oldservice.inject.ExtensionInjectionManager;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Internal configuration of an extension. */
public final class ExtensionSetup extends AbstractTreeNode<ExtensionSetup> {

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for setting the private field Extension#setup. */
    private static final VarHandle VH_EXTENSION_SETUP = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "setup",
            ExtensionSetup.class);

    /** The container where the extension is used. */
    public final ContainerSetup container;

    /** The extension realm this extension is a part of. */
    public final ExtensionTreeSetup extensionRealm;

    /** The type of extension. */
    public final Class<? extends Extension<?>> extensionType;

    /** The extension's injection manager. */
    public final ExtensionInjectionManager injectionManager;

    /** The extension instance that is exposed to users, instantiated and set in {@link #initialize()}. */
    @Nullable
    private Extension<?> instance;

    /** A static model of the extension. */
    public final ExtensionModel model;

    /**
     * Creates a new extension setup.
     * 
     * @param parent
     *            any parent this extension might have, null if the root extension
     * @param container
     *            the container this extension belongs to
     * @param extensionType
     *            the type of extension this setup class represents
     */
    ExtensionSetup(@Nullable ExtensionSetup parent, ContainerSetup container, Class<? extends Extension<?>> extensionType) {
        super(parent);
        this.container = requireNonNull(container);
        this.extensionType = requireNonNull(extensionType);
        if (parent == null) {
            this.extensionRealm = new ExtensionTreeSetup(this, extensionType);
            this.injectionManager = new ExtensionInjectionManager(null);
        } else {
            this.extensionRealm = parent.extensionRealm;
            this.injectionManager = new ExtensionInjectionManager(parent.injectionManager);
        }
        this.model = requireNonNull(extensionRealm.extensionModel);
    }

    void initialize() {
        instance = model.newInstance(this);

        // Add the extension to the container's map of extensions
        container.extensions.put(extensionType, this);

        // Hvad hvis en extension linker en af deres egne assemblies.
        // If the extension is added in the root container of an assembly. We need to add it there
        if (container.assembly.container == container) {
            container.assembly.extensions.add(this);
        }

        // Invoke Extension#onNew() before returning the new extension to the end-user
        try {
            MH_EXTENSION_ON_NEW.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    /**
     * Returns the extension instance.
     * 
     * @return the extension instance
     * @throws InternalExtensionException
     *             if trying to call this method from the constructor of the extension
     */
    public Extension<?> instance() {
        Extension<?> e = instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of an extension");
        }
        return e;
    }

    /**
     * Returns Extension#setup, is mainly used for core extensions that need special functionality
     * 
     * @param extension
     *            the extension to crack
     * @return the extension setup
     */
    public static ExtensionSetup crack(Extension<?> extension) {
        return (ExtensionSetup) VH_EXTENSION_SETUP.get(extension);
    }
}

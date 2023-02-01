package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.HashMap;

import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.Nullable;
import internal.app.packed.service.ExtensionServiceManager;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Internal configuration of an extension. */
public final class ExtensionSetup extends AbstractTreeNode<ExtensionSetup> implements Comparable<ExtensionSetup> {

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for setting the private field Extension#setup. */
    private static final VarHandle VH_EXTENSION_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), Extension.class, "extension", ExtensionSetup.class);

    /** A map of all non-void bean classes. Used for controlling non-multi-install beans. */
    public final HashMap<Class<?>, Object> beanClassMap = new HashMap<>();

    /** The container where the extension is used. */
    public final ContainerSetup container;

    /** The extension realm this extension is a part of. */
    public final ExtensionTreeSetup extensionTree;

    /** The type of extension. */
    public final Class<? extends Extension<?>> extensionType;

    /** The extension instance that is exposed to users, instantiated and set in {@link #initialize()}. */
    @Nullable
    private Extension<?> instance;

    /** A static model of the extension. */
    public final ExtensionModel model;

    /** The extension's injection manager. */
    public final ExtensionServiceManager sm;

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
    private ExtensionSetup(@Nullable ExtensionSetup parent, ContainerSetup container, Class<? extends Extension<?>> extensionType) {
        super(parent);
        this.container = requireNonNull(container);
        this.extensionType = requireNonNull(extensionType);
        if (parent == null) {
            this.extensionTree = new ExtensionTreeSetup(this, extensionType);
            this.sm = new ExtensionServiceManager(null);
        } else {
            this.extensionTree = parent.extensionTree;
            this.sm = new ExtensionServiceManager(parent.sm);
        }
        this.model = requireNonNull(extensionTree.model);
    }

    void close() {
        // Close all children first
        for (ExtensionSetup child = treeFirstChild; child != null; child = child.treeNextSiebling) {
            child.close();
        }

        sm.resolve(this);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ExtensionSetup o) {
        ExtensionModel otherModel = o.model;

        // First we compare the depth of each extension
        int d = model.orderingDepth() - otherModel.orderingDepth();
        if (d != 0) {
            return d;
        }

        // Then we compare the full name (class.getCanonicalName());

        int c = model.fullName().compareTo(otherModel.fullName());
        if (c != 0) {
            return c;
        }

        // Same canonical name, sort in order of use
        return extensionTree.usageOrderId - o.extensionTree.usageOrderId;
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

    public static ExtensionSetup initalizeExtension(Extension<?> instance) {
        ExtensionModel.Wrapper wrapper = ExtensionModel.CONSTRUCT.get();
        if (wrapper == null) {
            throw new UnsupportedOperationException("An extension instance cannot be created outside of use(Class<? extends Extension> extensionClass)");
        }
        ExtensionSetup s = wrapper.setup;
        if (s == null) {
            throw new IllegalStateException();
        }
        wrapper.setup = null;
        return s;
    }

    static ExtensionSetup install(Class<? extends Extension<?>> extensionType, ContainerSetup container, ExtensionSetup requestedByExtension) {
        // The extension must be recursively installed into all ancestors (if not already installed)
        ExtensionSetup extensionParent = container.treeParent == null ? null : container.treeParent.useExtension(extensionType, requestedByExtension);

        ExtensionSetup extension = new ExtensionSetup(extensionParent, container, extensionType);

        Extension<?> instance = extension.instance = extension.model.newInstance(extension);

        // Add the extension to the container's map of extensions
        container.extensions.put(extensionType, extension);

        // Hvad hvis en extension linker en af deres egne assemblies.
        // If the extension is added in the root container of an assembly. We need to add it there

        boolean isAssemblyRoot = container.assembly.container == null;
        if (isAssemblyRoot) {
            container.assembly.extensions.add(extension);
        }

        // Invoke Extension#onNew() before returning the extension/extension-point
        try {
            MH_EXTENSION_ON_NEW.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return extension;
    }
}

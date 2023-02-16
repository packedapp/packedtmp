package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.container.Realm;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.InternalExtensionException;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanOwner;
import internal.app.packed.service.ServiceManager;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * Internal configuration of an extension.
 * <p>
 * All extensions in an application is ordered in a tree which is why this class extends {@link AbstractTreeNode}.
 * <p>
 * This class implements {@link Comparable} in order to provide a deterministic order between extensions in the same
 * container.
 */
public final class ExtensionSetup extends AbstractTreeNode<ExtensionSetup> implements BeanOwner , Comparable<ExtensionSetup> {

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_BEAN_INTROSPECTOR = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class,
            "newBeanIntrospector", BeanIntrospector.class);

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_APPLICATION_CLOSE = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "onApplicationClose",
            void.class);

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_ASSEMBLY_CLOSE = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "onAssemblyClose",
            void.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_EXTENSION_MIRROR = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "newExtensionMirror",
            ExtensionMirror.class);

    /** A handle for setting the private field Extension#setup. */
    private static final VarHandle VH_EXTENSION_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), Extension.class, "extension", ExtensionSetup.class);

    /** The container where the extension is used. */
    public final ContainerSetup container;

    /** The type of extension. */
    public final Class<? extends Extension<?>> extensionType;

    /** The extension instance that is exposed to users, instantiated and set in {@link #initialize()}. */
    @Nullable
    private Extension<?> instance;

    /** A static model of the extension. */
    public final ExtensionModel model;

    /** The extension's injection manager. */
    public final ServiceManager sm;

    /** The extension realm this extension is a part of. */
    private final ExtensionTree tree;

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
            this.tree = new ExtensionTree(container, extensionType);
            this.sm = new ServiceManager(null, null);
        } else {
            this.tree = parent.tree;
            this.sm = new ServiceManager(parent.sm, null);
        }
        this.model = requireNonNull(tree.model);
    }

    void closeApplication() {
        tree.isConfigurable = false;

        try {
            MH_EXTENSION_ON_APPLICATION_CLOSE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        closeApplication0();
    }

    private void closeApplication0() {
        // Close all children first
        for (ExtensionSetup child = treeFirstChild; child != null; child = child.treeNextSiebling) {
            child.closeApplication0();
        }

        sm.verify();
    }

    void closeAssembly() {
        try {
            MH_EXTENSION_ON_ASSEMBLY_CLOSE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
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
        return tree.applicationExtensionId - o.tree.applicationExtensionId;
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

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return tree.isConfigurable;
    }

    /**
     * Call into {@link Extension#newBeanIntrospector()} to generate a new {@link BeanIntrospector}.
     *
     * @return the introspector
     */
    public BeanIntrospector newBeanIntrospector() {
        BeanIntrospector bi;
        try {
            bi = (BeanIntrospector) MH_EXTENSION_NEW_BEAN_INTROSPECTOR.invokeExact(instance());
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        if (bi == null) {
            throw new InternalExtensionException("newBeanIntrospector returned null for " + this);
        }
        return bi;
    }

    public ExtensionMirror<?> newExtensionMirror(Class<? extends ExtensionMirror<?>> mirrorClass) {
        ExtensionMirror<?> mirror;
        try {
            mirror = (ExtensionMirror<?>) MH_NEW_EXTENSION_MIRROR.invokeExact(instance());
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        if (mirror == null) {
            throw new InternalExtensionException("Extension " + model.fullName() + " returned null from " + model.name() + ".newExtensionMirror()");
        }
        // Check that the right extension mirror type was returned
        if (!mirrorClass.isInstance(mirror)) {
            throw new InternalExtensionException(extensionType.getSimpleName() + ".newExtensionMirror() was expected to return an instance of " + mirrorClass
                    + ", but returned an instance of " + mirror.getClass());
        }

        return mirror;
    }

    /** {@inheritDoc} */
    @Override
    public Realm realm() {
        return tree.model.realm();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Extension: " + extensionType.getCanonicalName();
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

    /**
     * Installs a new extension in the specified container.
     *
     * @param extensionType
     *            the type of extension to install
     * @param container
     *            the container to install the container in
     * @param requestedByExtension
     *            the extension that requested the extension or null if user
     * @return the new extension
     */
    static ExtensionSetup install(Class<? extends Extension<?>> extensionType, ContainerSetup container, @Nullable ExtensionSetup requestedByExtension) {
        // The extension must be recursively installed into all ancestors (if not already installed)
        ExtensionSetup extensionParent = container.treeParent == null ? null : container.treeParent.useExtension(extensionType, requestedByExtension);

        ExtensionSetup extension = new ExtensionSetup(extensionParent, container, extensionType);

        Extension<?> instance = extension.instance = extension.model.newInstance(extension);

        // Add the extension to the container's map of extensions
        container.extensions.put(extensionType, extension);

        // Hvad hvis en extension linker en af deres egne assemblies.
        // If the extension is added in the root container of an assembly. We need to add it there

        boolean isAssemblyRoot = container.treeParent == null || container.assembly.container == container;

        if (isAssemblyRoot) {
            container.assembly.extensions.add(extension);
        }

        // Invoke Extension#onNew() before returning the extension
        try {
            MH_EXTENSION_ON_NEW.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return extension;
    }

    /** A single instance of this class exists per extension per application. */
    private static final class ExtensionTree {

        /**
         * The extension id, this id may be used when ordering extensions. If there are multiple extensions with the same
         * canonically name and extension depth.
         */
        private final int applicationExtensionId;

        /** Whether or not this type of extension is still configurable. */
        private boolean isConfigurable = true;

        /** A model of the extension. */
        private final ExtensionModel model;

        /**
         * Creates a new realm.
         * <p>
         * This constructor is called from the constructor of the specified root
         *
         * @param root
         *            the root extension
         * @param extensionType
         *            the type of extension
         */
        private ExtensionTree(ContainerSetup container, Class<? extends Extension<?>> extensionType) {
            this.model = ExtensionModel.of(extensionType);
            this.applicationExtensionId = container.application.extensionId++;
        }
    }
}

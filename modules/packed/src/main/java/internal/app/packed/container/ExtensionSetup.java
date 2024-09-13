package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.component.Authority;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.InternalExtensionException;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.service.ServiceNamespaceSetup;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.MagicInitializer;
import internal.app.packed.util.ThrowableUtil;

/**
 * Internal configuration of an extension.
 * <p>
 * All extensions in an application is ordered in a tree which is why this class extends {@link AbstractTreeNode}.
 * <p>
 * This class implements {@link Comparable} in order to provide a deterministic order between extensions in the same
 * container.
 */
public final class ExtensionSetup extends AbstractTreeNode<ExtensionSetup> implements BuildLocalSource , AuthoritySetup , Comparable<ExtensionSetup> {

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_BEAN_INTROSPECTOR = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class,
            "newBeanIntrospector", BeanIntrospector.class);

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_EXTENSION_MIRROR = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "newExtensionMirror",
            ExtensionMirror.class);

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_APPLICATION_CLOSE = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "onApplicationClose",
            void.class);

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_ASSEMBLY_CLOSE = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "onAssemblyClose",
            void.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.findVirtual(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** Used for initializing {@link Extension}. */
    public static final MagicInitializer<ExtensionSetup> MAGIC_INITIALIZER = MagicInitializer.of(Extension.class);

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
    public final ServiceNamespaceSetup sm;

    /** The extension realm this extension is a part of. */
    public final ExtensionTree tree;

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
            this.tree = new ExtensionTree(container.application, extensionType);
            this.sm = new ServiceNamespaceSetup(null, null);
        } else {
            this.tree = parent.tree;
            this.sm = new ServiceNamespaceSetup(parent.sm, null);
        }
        this.model = requireNonNull(tree.model);
    }

    public void closeApplication() {
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
        // TODO: What if we add containers on shutdowns? I think it should be okay
        for (ExtensionSetup child = treeFirstChild; child != null; child = child.treeNextSibling) {
            child.closeApplication0();
        }

        sm.verify();
    }

    /** Call {@link Extension#onAssemblyClose()}. */
    public void closeAssembly() {
        try {
            MH_EXTENSION_ON_ASSEMBLY_CLOSE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ExtensionSetup other) {
        ExtensionModel otherModel = other.model;
        if (other == this) {
            return 0;
        }
        // We need a total deterministic ordering of extensions

        // First we compare the depth of each extension
        int d = model.orderingDepth() - otherModel.orderingDepth();
        if (d == 0) {
            // Then the simple name of the extension
            d = model.name().compareTo(otherModel.name());
            if (d == 0) {
                // Then the full name of the extension
                d = model.fullName().compareTo(otherModel.fullName());
                if (d == 0) {
                    // Same canonical name but different class loaders.
                    // sort in order of usage
                    d = tree.applicationExtensionId - other.tree.applicationExtensionId;
                }
            }
        }
        return d;
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
     * Call {@link Extension#newBeanIntrospector()} to generate a new {@link BeanIntrospector}.
     *
     * @return the new introspector
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
            mirror = (ExtensionMirror<?>) MH_EXTENSION_NEW_EXTENSION_MIRROR.invokeExact(instance());
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
    public Authority authority() {
        return tree.model.realm();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Extension: " + extensionType.getCanonicalName();
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
        // Install the extension recursively into all ancestors in the same application (if not already installed)
        ExtensionSetup extensionParent = container.isApplicationRoot() ? null : container.node.parent.useExtension(extensionType, requestedByExtension);

        ExtensionSetup extension = new ExtensionSetup(extensionParent, container, extensionType);

        Extension<?> instance = extension.instance = extension.model.newInstance(extension);

        // Add the extension to the container's map of extensions
        container.extensions.put(extensionType, extension);

        if (container.isAssemblyRoot()) {
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

    /** {@return a map of locals for the bean} */
    @Override
    public BuildLocalMap locals() {
        return container.locals();
    }

    /** A single instance of this class exists per extension per application. */
    public static final class ExtensionTree {

        /**
         * The extension id. This id may be used when ordering extensions if there are multiple extensions with the same
         * canonically name and extension depth.
         */
        private final int applicationExtensionId;

        /** Whether or not this type of extension is still configurable. */
        private boolean isConfigurable = true;

        /** A model of the extension. */
        private final ExtensionModel model;

        public final String name;

        /**
         * Creates a new ExtensionTree.
         *
         * @param container
         *            the root container
         * @param extensionType
         *            the type of extension
         */
        private ExtensionTree(ApplicationSetup application, Class<? extends Extension<?>> extensionType) {
            this.applicationExtensionId = application.extensionIdCounter++;
            this.model = ExtensionModel.of(extensionType);
            String name = model.name();
            int suffix = 1;
            while (application.extensions.putIfAbsent(name, extensionType) != null) {
                name = model.name() + suffix++;
            }
            this.name = name;
        }
    }
}

package internal.app.packed.extension;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.InternalExtensionException;
import app.packed.util.Nullable;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.service.MainServiceNamespaceHandle;
import internal.app.packed.util.accesshelper.ExtensionAccessHandler;

/**
 * Internal configuration of an extension.
 * <p>
 * All extensions in an application is ordered in a tree which is why this class extends {@link AbstractTreeNode}.
 * <p>
 * This class implements {@link Comparable} in order to provide a deterministic order between extensions in the same
 * container.
 */
public final class ExtensionSetup extends AuthoritySetup<ExtensionSetup> implements BuildLocalSource, Comparable<ExtensionSetup> {

    /** The container where the extension is used. */
    public final ContainerSetup container;

    /** The type of extension. */
    public final Class<? extends Extension<?>> extensionType;

    /** The extension instance that is exposed to users, instantiated and set in {@link #initialize()}. */
    @Nullable
    private Extension<?> instance;

    /** A static model of the extension. */
    public final ExtensionClassModel model;

    /** The extension's injection manager. */
    private MainServiceNamespaceHandle sm;

    /** The extension realm this extension is a part of. */
    public final ExtensionInstanceTree tree;

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
        this(parent, container, extensionType, parent == null ? new ExtensionInstanceTree(container.application, extensionType) : parent.tree);
    }

    private ExtensionSetup(@Nullable ExtensionSetup parent, ContainerSetup container, Class<? extends Extension<?>> extensionType, ExtensionInstanceTree tree) {
        super(parent, tree.servicesToResolve);
        this.container = requireNonNull(container);
        this.extensionType = requireNonNull(extensionType);
        this.tree = requireNonNull(tree);
        this.model = requireNonNull(tree.model);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentRealm authority() {
        return tree.model.realm();
    }

    public void closeApplication() {
        ExtensionAccessHandler.instance().invokeExtensionOnAssemblyClose(instance);
        tree.isConfigurable = false;
        closeApplication0();
    }

    private void closeApplication0() {
        // Close all children first
        // TODO: What if we add containers on shutdowns? I think it should be okay
        for (ExtensionSetup child = treeFirstChild; child != null; child = child.treeNextSibling) {
            child.closeApplication0();
        }
        // Only resolve services when we are at the root extension
        if (treeParent == null) {
            resolve();
        }

    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ExtensionSetup other) {
        ExtensionClassModel otherModel = other.model;
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

    /** Call {@link Extension#onAssemblyClose()}. */
    public void invokeExtensionOnAssemblyClose() {
        container.onAssemblyClose(this);
        ExtensionAccessHandler.instance().invokeExtensionOnAssemblyClose(instance);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return tree.isConfigurable;
    }

    /** {@return a map of locals for the bean} */
    @Override
    public BuildLocalMap locals() {
        return container.locals();
    }

    public ExtensionMirror<?> newExtensionMirror(Class<? extends ExtensionMirror<?>> mirrorClass) {
        ExtensionMirror<?> mirror = ExtensionAccessHandler.instance().invokeExtensionNewExtensionMirror(instance());
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

    public MainServiceNamespaceHandle services() {
        MainServiceNamespaceHandle s = sm;
        if (s == null) {
            MainServiceNamespaceHandle par = treeParent == null ? null : treeParent.services();
            ExtensionHandle<BaseExtension> eh = new PackedExtensionHandle<>(container.baseExtension());

            s = this.sm = eh.namespaceLazy(MainServiceNamespaceHandle.TEMPLATE, extensionType.getSimpleName() + "#main");
            s.init(par, container);
        }
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Extension: " + extensionType.getCanonicalName();
    }

    public static ExtensionSetup crack(Extension<?> extension) {
        requireNonNull(extension, "extension is null");
        return ExtensionAccessHandler.instance().getExtensionHandle(extension);
    }

    public static PackedExtensionPointHandle crack(ExtensionPoint<?> extensionPoint) {
        requireNonNull(extensionPoint, "extensionPoint is null");
        return ExtensionAccessHandler.instance().getExtensionPointPackedExtensionUseSite(extensionPoint);
    }

    /**
     * Create a new extension in the specified container.
     *
     * @param extensionType
     *            the type of extension to install
     * @param container
     *            the container to install the container in
     * @param requestedByExtension
     *            the extension that requested the extension or null if user
     * @return the new extension
     */
    public static ExtensionSetup newExtension(Class<? extends Extension<?>> extensionType, ContainerSetup container,
            @Nullable ExtensionSetup requestedByExtension) {
        // Install the extension recursively into all container ancestors in the same application (if not already installed)
        ExtensionSetup extensionParent = container.isApplicationRoot() ? null : container.treeParent.useExtension(extensionType, requestedByExtension);

        // Create the new extension
        ExtensionSetup extension = new ExtensionSetup(extensionParent, container, extensionType);

        // Create the new extension instance, using a MethodHandle
        Extension<?> instance = extension.instance = extension.model.factory.create(extension);

        // Add the extension to the container's map of extensions
        container.extensions.put(extensionType, extension);

        // If the container is the root of an assembly, add it there as well
        if (container.isAssemblyRoot()) {
            container.assembly.extensions.add(extension);
        }

        // Invoke Extension#onNew() before returning the extension
        ExtensionAccessHandler.instance().invokeExtensionOnNew(instance);
        return extension;
    }
}

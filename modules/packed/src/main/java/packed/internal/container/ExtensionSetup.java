package packed.internal.container;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import app.packed.application.ApplicationDescriptor;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.container.Composer;
import app.packed.container.ComposerAction;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionSupport;
import app.packed.extension.InternalExtensionException;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Build-time configuration of an extension. Exposed to end-users as {@link ExtensionConfiguration}. */
public final class ExtensionSetup implements ExtensionConfiguration {

    /** A handle for invoking the protected method {@link Extension#mirror()}. */
    private static final MethodHandle MH_EXTENSION_MIRROR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "mirror",
            ExtensionMirror.class);

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_APPLICATION_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onApplicationClose", void.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_USER_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onAssemblyClose",
            void.class);

    /** A handle for setting the private field Extension#context. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "setup",
            ExtensionSetup.class);

    /** The container where the extension is used. */
    public final ContainerSetup container;

    /** The type of extension that is being configured (copied form ExtenisonModel). */
    public final Class<? extends Extension<?>> extensionType;

    /** The extension instance, instantiated and set in {@link #initialize()}. */
    @Nullable
    private Extension<?> instance;

    /** The static model of the extension. */
    public final ExtensionModel model;

    /** Any parent extension this extension may have. Only the root extension in an application does not have a parent. */
    @Nullable
    public final ExtensionSetup parent;

    /** The (nullable) first child of the extension. */
    @Nullable
    public ExtensionSetup firstChild;

    /** The (nullable) last child of the extension. */
    @Nullable
    private ExtensionSetup lastChild;

    /** The (nullable) siebling of the extension. */
    @Nullable
    public ExtensionSetup siebling;

    /** The realm this extension belongs to. */
    private final ExtensionTreeSetup extensionTree;

    /** Beans, registered for this particular extension instance */
    public final ExtensionBeanServiceManager beans;

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
        this.container = requireNonNull(container);
        this.extensionType = requireNonNull(extensionType);
        this.parent = parent;
        if (parent == null) {
            this.extensionTree = new ExtensionTreeSetup(this, extensionType);
            this.beans = new ExtensionBeanServiceManager(null);
        } else {
            this.extensionTree = parent.extensionTree;
            this.beans = new ExtensionBeanServiceManager(parent.beans);

            // Extension tree maintenance
            if (parent.firstChild == null) {
                parent.firstChild = this;
            } else {
                parent.lastChild.siebling = this;
            }
            parent.lastChild = this;
        }
        this.model = requireNonNull(extensionTree.extensionModel);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDescriptor application() {
        return container.application.descriptor;
    }

    /** {@inheritDoc} */
    @Override
    public void checkExtensionConfigurable(Class<? extends Extension<?>> extensionType) {}

    /** {@inheritDoc} */
    @Override
    public void checkAssemblyConfigurable() {
        container.assembly.checkOpen();
    }

    /** {@inheritDoc} */
    @Override
    public <C extends Composer> void compose(C composer, ComposerAction<? super C> action) {
        action.build(composer);
    }

    void initialize() {
        // Creates a new extension instance
        instance = model.newInstance(this);

        // Set Extension.setup = this
        VH_EXTENSION_CONFIGURATION.set(instance, this);

        // Add the extension to the container's map of extensions
        container.extensions.put(extensionType, this);

        // Hvad hvis en extension linker en af deres egne assemblies.
        if (container.realm instanceof ContainerRealmSetup r && r.container() == container) {
            r.extensions.add(this);
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
    // This was previously a method on ExtensionConfiguration, and might become again one again if we want to extract some
    // tree info, otherwise we should be able to ditch the method, as useExtension() always makes the extension instance
    // has been properly initialized
    // I'm not sure we want to ever expose it via ExtensionContext... Users would need to insert a cast
    public Extension<?> instance() {
        Extension<?> e = instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of an extension");
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRootOfApplication() {
        return parent == null;
    }

    public ExtensionSetup applicationRootSetup() {
        ExtensionSetup s = this;
        while (s.parent != null) {
            s = s.parent;
        }
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionClass) {
        return container.isExtensionUsed(extensionClass);
    }

    /** {@return a mirror for the extension. An extension might specialize by overriding {@code Extension#mirror()}} */
    public ExtensionMirror mirror() {
        ExtensionMirror mirror = null;
        try {
            mirror = (ExtensionMirror) MH_EXTENSION_MIRROR.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        if (mirror == null) {
            throw new InternalExtensionException("Extension " + model.fullName() + " returned null from " + model.name() + ".mirror()");
        }
        return mirror;
    }

    /**
     * Invokes {@link Extension#onApplicationClose()}.
     * <p>
     * The extension is completed once the realm the container is part of is closed.
     */
    void onApplicationClose() {
        try {
            MH_EXTENSION_ON_APPLICATION_CLOSE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    void onUserClose() {
        try {
            MH_EXTENSION_ON_USER_CLOSE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    /** {@return the realm of this extension. This method will lazy initialize it.} */
    public ExtensionTreeSetup realm() {
        return extensionTree;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        requireNonNull(wireletClass, "wireletClass is null");

        // Check that we are a proper subclass of ExtensionWirelet
        ClassUtil.checkProperSubclass(Wirelet.class, wireletClass);

        // We only allow selection of wirelets in the same module as the extension itself
        // Otherwise people could do wirelets(ServiceWirelet.provide(..).getClass())...
        Module m = extensionType.getModule();
        if (m != wireletClass.getModule()) {
            throw new IllegalArgumentException("The specified wirelet class is not in the same module (" + m.getName() + ") as '"
                    + /* simple extension name */ model.name() + ", wireletClass.getModule() = " + wireletClass.getModule());
        }

        // Find the containers wirelet wrapper and return early if no wirelets have been specified, or all of them have already
        // been consumed
        WireletWrapper wirelets = container.wirelets;
        if (wirelets == null || wirelets.unconsumed() == 0) {
            return WireletSelection.of();
        }

        return new PackedWireletSelection<>(wirelets, wireletClass);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <E extends ExtensionSupport> E use(Class<E> supportClass) {
        requireNonNull(supportClass, "supportClass is null");

        // Finds the subtension's model and its extension class
        ExtensionSupportModel supportModel = ExtensionSupportModel.of(supportClass);
        Class<? extends Extension<?>> supportExtensionType = supportModel.extensionType();

        // Check that the requested subtension's extension is a direct dependency of this extension
        if (!model.dependencies().contains(supportExtensionType)) {
            // Special message if you try to use your own subtension
            if (extensionType == supportExtensionType) {
                throw new InternalExtensionException(extensionType.getSimpleName() + " cannot use its own support class " + supportExtensionType.getSimpleName()
                        + "." + supportClass.getSimpleName());
            }
            throw new InternalExtensionException(extensionType.getSimpleName() + " must declare " + format(supportExtensionType)
                    + " as a dependency in order to use " + supportExtensionType.getSimpleName() + "." + supportClass.getSimpleName());
        }

        // Get the extension instance (create it if needed) that the subtension needs
        Extension<?> instance = container.useExtensionSetup(supportExtensionType, this).instance;

        // Create a new subtension instance using the extension instance and this.extensionClass as the requesting extension
        return (E) supportModel.newInstance(instance, extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public NamespacePath containerPath() {
        return container.path();
    }

    static final class PreOrderIterator<T extends Extension<?>> implements Iterator<T> {

        /** The root extension. */
        private final ExtensionSetup root;

        /** The mapper that is applied on each node. */
        private final Function<ExtensionSetup, T> mapper;

        private ExtensionSetup next;

        PreOrderIterator(ExtensionSetup root, Function<ExtensionSetup, T> mapper) {
            this.root = this.next = root;
            this.mapper = mapper;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public T next() {
            ExtensionSetup n = next;
            if (n == null) {
                throw new NoSuchElementException();
            }

            if (n.firstChild != null) {
                next = n.firstChild;
            } else {
                next = findNext(n);
            }

            return mapper.apply(n);
        }

        private ExtensionSetup findNext(ExtensionSetup current) {
            if (current.siebling != null) {
                return current.siebling;
            }
            ExtensionSetup parent = current.parent;
            if (parent == root) {
                return null;
            } else {
                return findNext(parent);
            }
        }
    }
}

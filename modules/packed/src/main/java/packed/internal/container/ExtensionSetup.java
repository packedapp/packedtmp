package packed.internal.container;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.bean.hooks.BeanField;
import app.packed.bean.hooks.BeanInfo;
import app.packed.bean.hooks.BeanMethod;
import app.packed.container.Composer;
import app.packed.container.ComposerAction;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import app.packed.container.ExtensionPoint;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.operation.dependency.DependencyProvider;
import packed.internal.inject.ExtensionInjectionManager;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Build-time configuration of an extension. */
public final class ExtensionSetup {

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_HOOK_BEAN_BEGIN = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "hookOnBeanBegin",
            void.class, BeanInfo.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_HOOK_BEAN_DEPENDENCY_PROVIDER = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "hookOnBeanDependencyProvider", void.class, DependencyProvider.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_HOOK_BEAN_END = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "hookOnBeanEnd",
            void.class, BeanInfo.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_HOOK_BEAN_FIELD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "hookOnBeanField",
            void.class, BeanField.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_HOOK_BEAN_METHOD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "hookOnBeanMethod", void.class, BeanMethod.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_MIRROR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "newExtensionMirror",
            ExtensionMirror.class);

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_APPLICATION_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onApplicationClose", void.class);

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_ASSEMBLY_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onAssemblyClose", void.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for setting the private field Extension#setup. */
    private static final VarHandle VH_EXTENSION_SETUP = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "setup",
            ExtensionSetup.class);

    /** The (nullable) first child of the extension. */
    @Nullable
    public ExtensionSetup childFirst;

    /** The (nullable) last child of the extension. */
    @Nullable
    private ExtensionSetup childLast;

    /** The (nullable) siebling of the extension. */
    @Nullable
    public ExtensionSetup childSiebling;

    /** The container where the extension is used. */
    public final ContainerSetup container;

    /** The extension realm this extension is part of. */
    public final ExtensionRealmSetup extensionRealm;

    /** The type of extension that is being configured. */
    public final Class<? extends Extension<?>> extensionType;

    /** The extension's injection manager. */
    public final ExtensionInjectionManager injectionManager;

    /** The extension instance exposed to users, instantiated and set in {@link #initialize()}. */
    @Nullable
    private Extension<?> instance;

    /** A static model of the extension. */
    public final ExtensionModel model;

    /** Any parent extension this extension may have. Only the root extension in an application does not have a parent. */
    @Nullable
    public final ExtensionSetup parent;

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
            this.extensionRealm = new ExtensionRealmSetup(this, extensionType);
            this.injectionManager = new ExtensionInjectionManager(null);
        } else {
            this.extensionRealm = parent.extensionRealm;
            this.injectionManager = new ExtensionInjectionManager(parent.injectionManager);

            // Tree maintenance
            if (parent.childFirst == null) {
                parent.childFirst = this;
            } else {
                parent.childLast.childSiebling = this;
            }
            parent.childLast = this;
        }
        this.model = requireNonNull(extensionRealm.extensionModel);
    }

    public void checkConfigurable() {
        // Lots of combinations
        // User Assembly + User Container
        // User Assembly + Extension Container
        // Extension Assembly + Same Extension Assembly
        // Extension Assembly + Other Extension Assembly
        container.realm.checkOpen();
    }

    public <C extends Composer> void compose(C composer, ComposerAction<? super C> action) {
        action.build(composer);
    }

    public void hookOnBeanBegin(BeanInfo beanInfo) {
        try {
            MH_EXTENSION_HOOK_BEAN_BEGIN.invokeExact(instance, beanInfo);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    public void hookOnBeanEnd(BeanInfo beanInfo) {
        try {
            MH_EXTENSION_HOOK_BEAN_END.invokeExact(instance, beanInfo);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    public void hookOnBeanField(BeanField field) {
        try {
            MH_EXTENSION_HOOK_BEAN_FIELD.invokeExact(instance, field);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    public void hookOnBeanDependencyProvider(DependencyProvider provider) {
        try {
            MH_EXTENSION_HOOK_BEAN_DEPENDENCY_PROVIDER.invokeExact(instance, provider);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    public void hookOnBeanMethod(BeanMethod method) {
        try {
            MH_EXTENSION_HOOK_BEAN_METHOD.invokeExact(instance, method);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    void initialize() {
        // Creates a new extension instance
        instance = model.newInstance(this);

        // Set Extension.setup = this
        VH_EXTENSION_SETUP.set(instance, this);

        // Add the extension to the container's map of extensions
        container.extensions.put(extensionType, this);

        // Hvad hvis en extension linker en af deres egne assemblies.
        // If the extension is added in the root container of an assembly. We need to add it there
        if (container.realm instanceof AssemblySetup r && r.container() == container) {
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
    public Extension<?> instance() {
        Extension<?> e = instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of an extension");
        }
        return e;
    }

    /** {@return a mirror for the extension. An extension might specialize by overriding {@code Extension#mirror()}} */
    public ExtensionMirror<?> mirror() {
        ExtensionMirror<?> mirror = null;
        try {
            mirror = (ExtensionMirror<?>) MH_EXTENSION_MIRROR.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        if (mirror == null) {
            throw new InternalExtensionException("Extension " + model.fullName() + " returned null from " + model.name() + ".mirror()");
        }
        ExtensionMirrorModel.of(mirror.getClass());
        ExtensionMirrorModel.initialize(mirror, this);
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
            MH_EXTENSION_ON_ASSEMBLY_CLOSE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

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

    @SuppressWarnings("unchecked")
    public <E extends ExtensionPoint<?>> E use(Class<E> extensionPointType) {
        requireNonNull(extensionPointType, "extensionPointType is null");

        // Finds a model of the extension point
        ExtensionPointModel extensionPointModel = ExtensionPointModel.of(extensionPointType);
        Class<? extends Extension<?>> extensionPointExtensionType = extensionPointModel.extensionType();

        // Check that the requested extension point's extension is a direct dependency of this extension
        if (!model.dependencies().contains(extensionPointExtensionType)) {
            // Special message if you try to use your own extension point
            if (extensionType == extensionPointExtensionType) {
                throw new InternalExtensionException(extensionType.getSimpleName() + " cannot use its own extension point "
                        + extensionPointExtensionType.getSimpleName() + "." + extensionPointType.getSimpleName());
            }
            throw new InternalExtensionException(extensionType.getSimpleName() + " must declare " + format(extensionPointExtensionType)
                    + " as a dependency in order to use " + extensionPointExtensionType.getSimpleName() + "." + extensionPointType.getSimpleName());
        }

        // Get the extension instance (create it if needed) that the extension point is a part of
        ExtensionSetup extensionPoint = container.useExtensionSetup(extensionPointExtensionType, this);

        // Create the new extension point instance
        return (E) extensionPointModel.newInstance(extensionPoint, this);
    }

    /** A pre-order iterator for a rooted extension tree. */
    static final class PreOrderIterator<T extends Extension<?>> implements Iterator<T> {

        /** A mapper that is applied to each node. */
        private final Function<ExtensionSetup, T> mapper;

        /** The next extension, null if there are no next. */
        @Nullable
        private ExtensionSetup next;

        /** The root extension. */
        private final ExtensionSetup root;

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

            if (n.childFirst != null) {
                next = n.childFirst;
            } else {
                next = next(n);
            }

            return mapper.apply(n);
        }

        private ExtensionSetup next(ExtensionSetup current) {
            requireNonNull(current);
            if (current.childSiebling != null) {
                return current.childSiebling;
            }
            ExtensionSetup parent = current.parent;
            if (parent == root || parent == null) {
                return null;
            } else {
                return next(parent);
            }
        }
    }
}

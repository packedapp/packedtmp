package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.bean.BeanProcessor;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.inject.ExtensionInjectionManager;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Build-time configuration of an extension. */
public final class ExtensionSetup {

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_BEAN_SCANNER = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "newBeanScanner",
            BeanProcessor.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_BEAN_SCANNER_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BeanProcessor.class, "initialize",
            void.class, ExtensionSetup.class, BeanSetup.class);

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

    void initialize() {
        // Creates a new extension instance
        instance = model.newInstance(this);

        // Set Extension.setup = this
        VH_EXTENSION_SETUP.set(instance, this);

        // Add the extension to the container's map of extensions
        container.extensions.put(extensionType, this);

        // Hvad hvis en extension linker en af deres egne assemblies.
        // If the extension is added in the root container of an assembly. We need to add it there
        if (container.realm instanceof UserRealmSetup r && r.container() == container) {
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

    public BeanProcessor newBeanScanner(ExtensionSetup extension, BeanSetup bean) {
        BeanProcessor bs;
        try {
            bs = (BeanProcessor) MH_EXTENSION_NEW_BEAN_SCANNER.invokeExact(instance);
            MH_BEAN_SCANNER_INITIALIZE.invokeExact(bs, extension, bean);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return bs;
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
        // Check that we are a proper subclass of ExtensionWirelet
        ClassUtil.checkProperSubclass(Wirelet.class, wireletClass, "wireletClass");

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

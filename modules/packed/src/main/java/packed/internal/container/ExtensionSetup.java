package packed.internal.container;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;

import app.packed.application.ApplicationDescriptor;
import app.packed.base.Nullable;
import app.packed.component.RealmMirror;
import app.packed.container.Assembly;
import app.packed.container.Composer;
import app.packed.container.ComposerAction;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionSupport;
import app.packed.extension.InternalExtensionException;
import app.packed.extension.old.ExtensionBeanConnection;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Build-time configuration of an extension. Exposed to end-users as {@link ExtensionConfiguration}. */
public final class ExtensionSetup implements ExtensionConfiguration {

    /** A handle for invoking the protected method {@link Extension#mirror()}. */
    private static final MethodHandle MH_EXTENSION_MIRROR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "mirror",
            ExtensionMirror.class);

    /** A handle for invoking the protected method {@link Extension#onClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onClose", void.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for invoking the protected method {@link Extension#onUserClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_USER_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onUserClose",
            void.class);

    /** A handle for setting the private field Extension#context. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionSetup.class);

    /** The container where the extension is used. */
    public final ContainerSetup container;

    /** The type of extension that is being configured. */
    public final Class<? extends Extension> extensionType;

    /** The extension instance, instantiated and set in {@link #initialize()}. */
    @Nullable
    private Extension instance;

    /** The static model of the extension. */
    final ExtensionModel model;

    /** Any parent extension this extension may have. Only the root extension does not have a parent. */
    @Nullable
    public final ExtensionSetup parent;

    /** The realm this extension belongs to. */
    private final ExtensionRealmSetup realm;

    /**
     * Creates a new extension setup.
     * 
     * @param container
     *            the container this extension belongs to
     * @param model
     *            the model of the extension
     */
    ExtensionSetup(@Nullable ExtensionSetup parent, ContainerSetup container, Class<? extends Extension> extensionClass) {
        this.container = requireNonNull(container);
        this.parent = parent;
        if (parent == null) {
            this.realm = container.application.extensions.computeIfAbsent(extensionClass, ec -> new ExtensionRealmSetup(this, container.application, ec));
            this.model = requireNonNull(realm.extensionModel);
            this.extensionType = model.type();
        } else {
            this.realm = parent.realm;
            this.model = parent.model;
            this.extensionType = parent.extensionType;
        }
        // Find (or create) the extension realm for the application
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDescriptor application() {
        return container.application.descriptor;
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurableFor(Class<? extends Extension> extensionType) {}

    /** {@inheritDoc} */
    @Override
    public void checkUserConfigurable() {
        container.realm.checkOpen();
    }

    /** {@inheritDoc} */
    @Override
    public <C extends Composer> void compose(C composer, ComposerAction<? super C> action) {
        action.build(composer);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerMirror container() {
        return container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return container.depth;
    }

    /** {@inheritDoc} */
    @Override
    public <T> ExtensionBeanConnection<T> findAncestor(Class<T> type) {
        requireNonNull(type, "type is null");
        ContainerSetup parent = container.parent;
        while (parent != null) {
            ExtensionSetup extensionContext = parent.extensions.get(extensionType);
            if (extensionContext != null) {
                return PackedExtensionAncestor.sameApplication(extensionContext.instance);
            }
            // if (parentOnly) break;
            parent = parent.parent;
        }
        return ExtensionBeanConnection.empty();
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<ExtensionBeanConnection<T>> findParent(Class<T> type) {
        requireNonNull(type, "type is null");
        ContainerSetup parent = container.parent;
        if (parent != null) {
            ExtensionSetup extensionContext = parent.extensions.get(extensionType);
            if (extensionContext != null) {
                Extension instance = extensionContext.instance;
                if (type.isInstance(instance)) {
                    @SuppressWarnings("unchecked")
                    ExtensionBeanConnection<T> c = (ExtensionBeanConnection<T>) PackedExtensionAncestor.sameApplication(instance);
                    return Optional.of(c);
                }
            }
        }
        return Optional.empty();
    }

    void initialize() {
        // Creates a new extension instance, and set Extension.configuration = ExtensionSetup
        instance = model.newInstance(this);
        VH_EXTENSION_CONFIGURATION.set(instance, this);

        // Add the extension to the container's extension map as well as the (application-scoped) extension realm
        container.extensions.put(extensionType, this);

        // The extension has been now been fully wired, run any notifications
        // extension.onWired();
        //// IDK if we have another technique... Vi har snakket lidt om at have de der dybe hooks...

        // Finally, invoke Extension#onNew() after which the new extension can be returned to the end-user
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
    public Extension instance() {
        Extension e = instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of an extension");
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isApplicationRoot() {
        return parent == null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExtensionUsed(Class<? extends Extension> extensionClass) {
        return container.isExtensionUsed(extensionClass);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerMirror link(RealmMirror realm, Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
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
     * Invokes {@link Extension#onClose()}.
     * <p>
     * The extension is completed once the realm the container is part of is closed.
     */
    void onClose() {
        try {
            MH_EXTENSION_ON_CLOSE.invokeExact(instance);
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
    public ExtensionRealmSetup realm() {
        return realm;
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
        Class<? extends Extension> supportExtensionType = supportModel.extensionType();

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
        Extension instance = container.useExtension(supportExtensionType, this).instance;

        // Create a new subtension instance using the extension instance and this.extensionClass as the requesting extension
        return (E) supportModel.newInstance(instance, extensionType);
    }
}

//protected void attributesAdd(DefaultAttributeMap dam) {
//  PackedAttributeModel pam = model.attributes;
//  if (pam != null) {
//      pam.set(dam, instance);
//  }
//}

package packed.internal.container;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.container.Composer;
import app.packed.container.ComposerAction;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.extension.Extension;
import app.packed.extension.Extension.Subtension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.InternalExtensionException;
import app.packed.extension.old.ExtensionBeanConnection;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.attribute.PackedAttributeModel;
import packed.internal.component.RealmSetup;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Build-time configuration of an extension. Exposed to end-users as {@link ExtensionConfiguration}. */
public final class ExtensionSetup implements ExtensionConfiguration {

    /** The container where the extension is used. */
    public final ContainerSetup container;

    /** The type of extension that is being configured. */
    public final Class<? extends Extension> extensionType;

    /** The extension instance, instantiated and set in {@link #newExtension(ContainerSetup, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** Whether or not the extension has been configured. */
    private boolean isNew;

    /** The static model of the extension. */
    final ExtensionModel model;

    /**
     * The realm this extension belongs to, lazily initialized if needed, for example, if the extension installs its own
     * beans.
     */
    // Taenker ogsaa hooks maa tilhoere den...
    @Nullable
    private RealmSetup realm;

    /**
     * Creates a new extension setup.
     * 
     * @param container
     *            the container this extension belongs to
     * @param model
     *            the model of the extension
     */
    private ExtensionSetup(ContainerSetup container, ExtensionModel model) {
        this.container = requireNonNull(container);
        this.model = requireNonNull(model);
        this.extensionType = model.type();
    }

    protected void attributesAdd(DefaultAttributeMap dam) {
        PackedAttributeModel pam = model.attributes;
        if (pam != null) {
            pam.set(dam, instance);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkIsPreCompletion() {
        if (isConfigured) {
            throw new IllegalStateException("This extension (" + model.name() + ") is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkIsPreLinkage() {
        if (!isNew) {
            throw new IllegalStateException();
        }
//        // Ja og saa alligevel ikke. Hvis vi lige saa stille taeller ned...
//        // Og disable hver extension loebende
//        if (container.containerChildren != null) {
//            
//        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> ExtensionBeanConnection<T> findAncestor(Class<T> type) {
        requireNonNull(type, "type is null");
        ContainerSetup parent = container.containerParent;
        while (parent != null) {
            ExtensionSetup extensionContext = parent.extensions.get(extensionType);
            if (extensionContext != null) {
                return PackedExtensionAncestor.sameApplication(extensionContext.instance);
            }
            // if (parentOnly) break;
            parent = parent.containerParent;
        }
        return ExtensionBeanConnection.empty();
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<ExtensionBeanConnection<T>> findParent(Class<T> type) {
        requireNonNull(type, "type is null");
        ContainerSetup parent = container.containerParent;
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
    Extension instance() {
        Extension e = instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of an extension");
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPartOfImage() {
        return container.application.buildKind.isImage();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExtensionUsed(Class<? extends Extension> extensionClass) {
        return container.isExtensionUsed(extensionClass);
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
        return mirror;
    }

    /**
     * Invokes {@link Extension#onComplete()}.
     * <p>
     * The extension is completed once the realm the container is part of is closed.
     */
    void onComplete() {
        try {
            MH_EXTENSION_ON_COMPLETE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        isConfigured = true;
    }

    void preContainerChildren() {
        try {
            MH_EXTENSION_ON_PREEMBLE_COMPLETE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    /** {@return the realm of this extension. This method will lazy initialize it.} */
    public RealmSetup realm() {
        RealmSetup r = realm;
        if (r == null) {
            r = realm = new RealmSetup(this);
        }
        return r;
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
    public <E extends Subtension> E use(Class<E> subtensionClass) {
        requireNonNull(subtensionClass, "subtensionClass is null");

        // Finds the subtension's model and its extension class
        SubtensionModel subModel = SubtensionModel.of(subtensionClass);
        Class<? extends Extension> subExtensionType = subModel.extensionType();

        // Check that the requested subtension's extension is a direct dependency of this extension
        if (!model.dependencies().contains(subExtensionType)) {
            // Special message if you try to use your own subtension
            if (extensionType == subExtensionType) {
                throw new InternalExtensionException(extensionType.getSimpleName() + " cannot use its own subtension " + subExtensionType.getSimpleName() + "."
                        + subtensionClass.getSimpleName());
            }
            throw new InternalExtensionException(extensionType.getSimpleName() + " must declare " + format(subModel.extensionType())
                    + " as a dependency in order to use " + subExtensionType.getSimpleName() + "." + subtensionClass.getSimpleName());
        }

        // Get the extension instance (create it if needed) that the subtension needs
        Extension instance = container.useExtension(subExtensionType, this).instance;

        // Create a new subtension instance using the extension instance and this.extensionClass as the requesting extension
        return (E) subModel.newInstance(instance, extensionType);
    }

    /**
     * Create a new extension.
     * 
     * @param container
     *            the container to which the extension should be added
     * @param extensionClass
     *            the extension to create
     * @return the new extension
     */
    static ExtensionSetup newExtension(ContainerSetup container, Class<? extends Extension> extensionClass) {
        // Find extension model and create extension setup.
        ExtensionModel model = ExtensionModel.of(extensionClass);
        ExtensionSetup extension = new ExtensionSetup(container, model);

        // Creates a new extension instance, and set Extension.configuration = ExtensionSetup
        Extension instance = extension.instance = model.newInstance(extension);
        VH_EXTENSION_CONTEXT.set(instance, extension);

        // Add the extension to the container's extension map
        container.extensions.put(extensionClass, extension);

        // The extension has been now been fully wired, run any notifications
        // extension.onWired();
        //// IDK if we have another technique... Vi har snakket lidt om at have de der dybe hooks...

        // Finally, invoke Extension#onNew() after which the new extension can be returned to the end-user
        try {
            MH_EXTENSION_ON_NEW.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }

        return extension;
    }

    /** A handle for invoking the protected method {@link Extension#mirror()}. */
    private static final MethodHandle MH_EXTENSION_MIRROR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "mirror",
            ExtensionMirror.class);

    /** A handle for invoking the protected method {@link Extension#onComplete()}. */
    private static final MethodHandle MH_EXTENSION_ON_COMPLETE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onComplete",
            void.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for invoking the protected method {@link Extension#onPreChildren()}. */
    private static final MethodHandle MH_EXTENSION_ON_PREEMBLE_COMPLETE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onPreChildren", void.class);

    /** A handle for setting the private field Extension#context. */
    private static final VarHandle VH_EXTENSION_CONTEXT = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    @Override
    public <C extends Composer> void compose(C composer, ComposerAction<? super C> action) {
        throw new UnsupportedOperationException();
    }
}

//// Extensions do not support lookup objects...
//
///**
//* @param lookup
//*/
//// Do we actually want to support this??? IDK
//public void lookup(Lookup lookup) {
//  throw new UnsupportedOperationException();
//}

// Previously used for extract an extension from a component mirror
// Not needed anymore after mirrors
//public static ExtensionSetup extractExtensionSetup(MethodHandles.Lookup lookup, ComponentMirror containerComponent) {
//  requireNonNull(lookup, "containerComponent is null");
//
//  // We only allow to call in directly on the container itself
//  if (!containerComponent.modifiers().isContainer()) {
//      throw new IllegalArgumentException("The specified component '" + containerComponent.path() + "' must have the Container modifier, modifiers = "
//              + containerComponent.modifiers());
//  }
//
//  // lookup.lookupClass() must point to the extension that should be extracted
//  if (lookup.lookupClass() == Extension.class || !Extension.class.isAssignableFrom(lookup.lookupClass())) {
//      throw new IllegalArgumentException("The lookupClass() of the specified lookup object must be a proper subclass of "
//              + Extension.class.getCanonicalName() + ", was " + lookup.lookupClass());
//  }
//
//  @SuppressWarnings("unchecked")
//  Class<? extends Extension> extensionClass = (Class<? extends Extension>) lookup.lookupClass();
//  // Must have full access to the extension class
//  if (!lookup.hasFullPrivilegeAccess()) {
//      throw new IllegalArgumentException("The specified lookup object must have full privilege access to " + extensionClass
//              + ", try creating a new lookup object using MethodHandles.privateLookupIn(lookup, " + extensionClass.getSimpleName() + ".class)");
//  }
//
//  ContainerSetup container = (ContainerSetup) ComponentSetup.unadapt(lookup, containerComponent);
//  return container.extensions.get(extensionClass);
//}

///**
//* If need 2 sentinel values we can use both null and this. For example, null can mean uninitialized and this can mean
//* no ancestors
//* <p>
//* An ancestor is a direct parent if {@code ancestor.container == this.container.parent}.
//**/
//@Nullable
//final ExtensionSetup ancestor;

// Tror vi beregner ExtensionSetup on demand...

// Dvs hvis man vil vide om man er connected saa tager man det ind i constructuren...

//ExtensionSetup anc;
//if (model.extensionLinkedDirectChildrenOnly) {
//  if (container.containerParent != null) {
//      ancestor = container.containerParent.extensions.get(extensionType);
//  } else {
//      ancestor = null;
//  }
//} else {
//  ancestor = null;
//}
package packed.internal.container;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.ComponentAttributes;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;
import app.packed.component.WireletSource;
import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.InternalExtensionException;
import app.packed.inject.Factory;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.attribute.PackedAttributeModel;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedWireletSource;
import packed.internal.component.WireletWrapper;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** The internal configuration of an extension. Exposed to end-users as {@link ExtensionConfiguration}. */
// Lige nu beholder vi den som component...
public final class ExtensionSetup extends ComponentSetup implements ExtensionConfiguration {

    /** A handle for invoking {@link Extension#onComplete()}. */
    private static final MethodHandle MH_EXTENSION_ON_COMPLETE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onComplete",
            void.class);

    /** A handle for invoking {@link Extension#onNew()}, used by {@link #newInstance(ContainerSetup, Class)}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    private static final MethodHandle MH_EXTENSION_ON_PREEMBLE_COMPLETE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onPreembleComplete", void.class);

    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    static final MethodHandle MH_INJECT_PARENT = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ExtensionSetup.class, "injectParent", Extension.class);

    /** A handle for setting the field Extension#configuration, used by {@link #newInstance(ContainerSetup, Class)}. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    /** The container this extension is part of. */
    public final ContainerSetup container;

    /** The extension instance, instantiated in {@link #newExtension(ContainerSetup, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** This extension's model. */
    final ExtensionModel model;

    /**
     * Creates a new extension setup.
     * 
     * @param container
     *            the container this extension belongs to
     * @param model
     *            the model of the extension
     */
    private ExtensionSetup(ContainerSetup container, ExtensionModel model) {
        super(container, model);
        this.container = requireNonNull(container);
        this.model = requireNonNull(model);
    }

    /** {@inheritDoc} */
    @Override
    protected void attributesAdd(DefaultAttributeMap dam) {
        dam.addValue(ComponentAttributes.EXTENSION_TYPE, extensionClass());
        PackedAttributeModel pam = model.attributes;
        if (pam != null) {
            pam.set(dam, instance);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkExtendable() {
        // Ja og saa alligevel ikke. Hvis vi lige saa stille taeller ned...
        // Og disable hver extension loebende
        if (container.containerChildren != null) {
            throw new IllegalStateException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkIsBuilding() {
        if (isConfigured) {
            throw new IllegalStateException("This extension (" + model.name() + ") is no longer configurable");
        }
    }

    /** {@return the extension class.} */
    private Class<? extends Extension> extensionClass() {
        return model.type();
    }

    /** {@inheritDoc} */
    public Extension extensionInstance() {
        Extension e = instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of an extension");
        }
        return e;
    }

    /**
     * Used by {@link #MH_INJECT_PARENT}.
     * 
     * @return the parent extension instance
     */
    Extension injectParent() {
        ContainerSetup parent = container.containerParent;
        if (parent != null) {
            ExtensionSetup extensionContext = parent.extensions.get(extensionClass());
            if (extensionContext != null) {
                return extensionContext.instance;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Class<?> implementation) {
        return container.wire(ComponentDriver.driverInstall(implementation), realm);
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Factory<?> factory) {
        return container.wire(ComponentDriver.driverInstall(factory), realm);
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration installInstance(Object instance) {
        return container.wire(ComponentDriver.driverInstallInstance(instance), realm);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPartOfImage() {
        return application.isImage();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUsed(Class<? extends Extension> extensionClass) {
        return container.isUsed(extensionClass);
    }

    /**
     * @param lookup
     */
    // Do we actually want to support this??? IDK
    public void lookup(Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    /**
     * The extension is completed once the realm the container is part of is closed. Will invoke
     * {@link Extension#onComplete()}.
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

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <E extends Subtension> E use(Class<E> subtensionClass) {
        requireNonNull(subtensionClass, "subtensionClass is null");

        // Finds the subtension's model and its extension class
        SubtensionModel subModel = SubtensionModel.of(subtensionClass);
        Class<? extends Extension> subExtensionClass = subModel.extensionClass();

        // Check that the requested subtension's extension is a direct dependency of this extension
        if (!model.dependencies().contains(subExtensionClass)) {
            // Special message if you try to use your own subtension
            if (extensionClass() == subExtensionClass) {
                throw new InternalExtensionException(extensionClass().getSimpleName() + " cannot use its own subtension " + subExtensionClass.getSimpleName()
                        + "." + subtensionClass.getSimpleName());
            }
            throw new InternalExtensionException(extensionClass().getSimpleName() + " must declare " + format(subModel.extensionClass())
                    + " as a dependency in order to use " + subExtensionClass.getSimpleName() + "." + subtensionClass.getSimpleName());
        }

        // Get the extension instance (create it if needed) that the subtension needs
        Extension instance = container.useExtension(subExtensionClass, this).instance;

        // Create a new subtension instance using the extension instance and this.extensionClass as the requesting extension
        return (E) subModel.newInstance(instance, extensionClass());
    }

    /** {@inheritDoc} */
    @Override
    public <C extends ComponentConfiguration> C userWire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return container.wire(driver, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Wirelet> WireletSource<T> wirelets(Class<T> wireletClass) {
        requireNonNull(wireletClass, "wireletClass is null");

        // We only allow consummation of wirelets in the same module as the extension class
        // Otherwise people would be able to use something like wirelets(ServiceWirelet.provide(..).getClass()).consumeAll
        Module m = extensionClass().getModule();
        if (m != wireletClass.getModule()) {
            throw new InternalExtensionException("Must specify a wirelet class that is in the same module (" + m.getName() + ") as '" + model.name()
                    + ", wireletClass.getModule() = " + wireletClass.getModule());
        }

        // The extension does not store any wirelets itself, fetch them from the extension's container instead
        WireletWrapper wirelets = container.wirelets;
        if (wirelets == null || wirelets.unconsumed() == 0) {
            return WireletSource.of();
        }
        return new PackedWireletSource<>(wirelets, wireletClass);
    }

    public static ExtensionSetup extractExtensionSetup(MethodHandles.Lookup lookup, ComponentMirror containerComponent) {
        requireNonNull(lookup, "containerComponent is null");

        // We only allow to call in directly on the container itself
        if (!containerComponent.modifiers().isContainer()) {
            throw new IllegalArgumentException("The specified component '" + containerComponent.path() + "' must have the Container modifier, modifiers = "
                    + containerComponent.modifiers());
        }

        // lookup.lookupClass() must point to the extension that should be extracted
        if (lookup.lookupClass() == Extension.class || !Extension.class.isAssignableFrom(lookup.lookupClass())) {
            throw new IllegalArgumentException("The lookupClass() of the specified lookup object must be a proper subclass of "
                    + Extension.class.getCanonicalName() + ", was " + lookup.lookupClass());
        }

        @SuppressWarnings("unchecked")
        Class<? extends Extension> extensionClass = (Class<? extends Extension>) lookup.lookupClass();
        // Must have full access to the extension class
        if (!lookup.hasFullPrivilegeAccess()) {
            throw new IllegalArgumentException("The specified lookup object must have full privilege access to " + extensionClass
                    + ", try creating a new lookup object using MethodHandles.privateLookupIn(lookup, " + extensionClass.getSimpleName() + ".class)");
        }

        ContainerSetup container = (ContainerSetup) ComponentSetup.unadapt(lookup, containerComponent);
        return container.extensions.get(extensionClass);
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
        VH_EXTENSION_CONFIGURATION.set(instance, extension);

        // Add the extension to the container's extension map
        container.extensions.put(extensionClass, extension);

        // The extension has been now been fully wired, run any notifications
        // extension.onWired();

        //// IDK if we have another technique... Vi har snakket lidt om at have de der dybe hooks...

        // Finally, invoke Extension#onNew() before returning the new extension to the end-user
        try {
            MH_EXTENSION_ON_NEW.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }

        return extension;
    }
}

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
//      ancestor = container.containerParent.extensions.get(extensionClass());
//  } else {
//      ancestor = null;
//  }
//} else {
//  ancestor = null;
//}
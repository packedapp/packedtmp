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
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.InternalExtensionException;
import app.packed.inject.Factory;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.attribute.PackedAttributeModel;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedWireletHandle;
import packed.internal.component.WireletWrapper;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** The internal configuration of an extension. Exposed to end-users as {@link ExtensionConfiguration}. */
public final class ExtensionSetup extends ComponentSetup implements ExtensionConfiguration {

    /** A handle for invoking {@link Extension#onComplete()}. */
    private static final MethodHandle MH_EXTENSION_ON_COMPLETE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onComplete",
            void.class);

    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    private static final MethodHandle MH_EXTENSION_ON_CONTAINER_LINKAGE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onExtensionsFixed", void.class);

    /** A handle for invoking {@link Extension#onNew()}, used by {@link #initialize(ContainerSetup, Class)}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    static final MethodHandle MH_INJECT_PARENT = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ExtensionSetup.class, "injectParent", Extension.class);

    /** A handle for setting the field Extension#configuration, used by {@link #initialize(ContainerSetup, Class)}. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    /** The extension instance, instantiated in {@link #initialize(ContainerSetup, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** A model of the extension. */
    private final ExtensionModel model;
    
    /**
     * Creates a new extension setup.
     * 
     * @param container
     *            the container this extension belongs to part
     * @param model
     *            a model of the extension
     */
    private ExtensionSetup(ContainerSetup container, ExtensionModel model) {
        super(container, model);
        this.model = requireNonNull(model);
    }

    /** {@inheritDoc} */
    @Override
    protected void addAttributes(DefaultAttributeMap dam) {
        dam.addValue(ComponentAttributes.EXTENSION_CLASS, extensionClass());
        PackedAttributeModel pam = model.attributes;
        if (pam != null) {
            pam.set(dam, instance);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (isConfigured) {
            throw new IllegalStateException("This extension (" + model.name() + ") is no longer configurable");
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
    public Class<? extends Extension> extensionClass() {
        return model.extensionClass();
    }

    /** {@inheritDoc} */
    public Extension extensionInstance() {
        Extension e = instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of " + model.fullName());
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
            ExtensionSetup extensionContext = parent.getExtensionContext(extensionClass());
            if (extensionContext != null) {
                return extensionContext.instance;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Class<?> implementation) {
        return wire(ComponentDriver.driverInstall(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Factory<?> factory) {
        return wire(ComponentDriver.driverInstall(factory));
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration installInstance(Object instance) {
        return wire(ComponentDriver.driverInstallInstance(instance));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPartOfImage() {
        return container.isPartOfImage();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUsed(Class<? extends Extension> extensionClass) {
        return container.isInUse(extensionClass);
    }

    /**
     * @param lookup
     */
    // Do we actually want to support this??? IDK
    public void lookup(Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    /** {@return the extension's model} */
    public ExtensionModel model() {
        return model;
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
            MH_EXTENSION_ON_CONTAINER_LINKAGE.invokeExact(instance);
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
        Class<? extends Extension> subExtensionClass = subModel.extensionClass;

        // Check that the requested subtension's extension is a direct dependency of this extension
        if (!model.dependencies().contains(subExtensionClass)) {
            // Special message if you try to use your own subtension
            if (model.extensionClass() == subExtensionClass) {
                throw new InternalExtensionException(model.extensionClass().getSimpleName() + " cannot use its own subtension "
                        + subExtensionClass.getSimpleName() + "." + subtensionClass.getSimpleName());
            }
            throw new InternalExtensionException(model.extensionClass().getSimpleName() + " must declare " + format(subModel.extensionClass)
                    + " as a dependency in order to use " + subExtensionClass.getSimpleName() + "." + subtensionClass.getSimpleName());
        }

        // Get the extension instance (create it if needed) that the subtension needs
        Extension instance = container.useDependencyCheckedExtension(subExtensionClass, this).instance;

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
    public <T extends Wirelet> PackedWireletHandle<T> wirelets(Class<T> wireletClass) {
        requireNonNull(wireletClass, "wireletClass is null");

        // We only allow consummation of wirelets in the same module as the extension
        Module m = model.extensionClass().getModule();
        if (m != wireletClass.getModule()) {
            throw new InternalExtensionException("Must specify a wirelet class that is in the same module (" + m.getName() + ") as '" + model.name()
                    + ", wireletClass.getModule() = " + wireletClass.getModule());
        }
        // The extension does not store any wirelets itself, fetch them from the extension's container instead
        WireletWrapper wirelets = container.wirelets;
        if (wirelets == null || wirelets.unconsumed() == 0) {
            return PackedWireletHandle.of();
        }
        return new PackedWireletHandle<>(wirelets, wireletClass);
    }

    /**
     * Instantiation the extension and create a new extension setup.
     * 
     * @param container
     *            the container setup
     * @param extensionClass
     *            the extension to instantiate
     * @return the new setup
     */
    static ExtensionSetup initialize(ContainerSetup container, Class<? extends Extension> extensionClass) {
        // Find extension model and create extension setup.
        ExtensionModel model = ExtensionModel.of(extensionClass);
        ExtensionSetup extension = new ExtensionSetup(container, model);

        // Creates a new extension instance
        Extension instance = extension.instance = model.newInstance(extension);
        VH_EXTENSION_CONFIGURATION.set(instance, extension); // sets Extension.configuration = extension (setup)

        // Invoke Extension#onNew()
        try {
            MH_EXTENSION_ON_NEW.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }

        return extension;
    }
}

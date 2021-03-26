package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

public final class NewExtensionSetup extends ComponentSetup {
    
    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    private static final MethodHandle MH_EXTENSION_ON_CONTAINER_LINKAGE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onExtensionsFixed", void.class);

    /** A handle for invoking {@link Extension#onNew()}, used by {@link #initialize(ContainerSetup, Class)}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle for accessing the field Extension#configuration, used by {@link #initialize(ContainerSetup, Class)}. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);
    
    /** A model of the extension. */
    public final ExtensionModel model;

    /** The extension setup if this component represents an extension, otherwise null. */
    @Nullable
    public final ExtensionSetup extension;

    /** The extension instance, instantiated in {@link #initialize(ContainerSetup, Class)}. */
    @Nullable
    Extension instance;

    public NewExtensionSetup(ComponentSetup parent, ExtensionModel model) {
        super(parent, model);
        this.model = requireNonNull(model);
        this.extension = new ExtensionSetup(this, model);

        setName0(null /* model.nameComponent */); // setName0(String) does not work currently
    }

    void preContainerChildren() {
        try {
            MH_EXTENSION_ON_CONTAINER_LINKAGE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }
    

    /**
     * Create and initialize a new extension.
     * 
     * @param container
     *            the container setup
     * @param extensionClass
     *            the extension to initialize
     * @return a setup for the extension
     */
    static ExtensionSetup initialize(ContainerSetup container, Class<? extends Extension> extensionClass) {
        // Find extension model and create setups.
        ExtensionModel model = ExtensionModel.of(extensionClass);
        NewExtensionSetup component = new NewExtensionSetup(container.component, model); // creates ExtensionSetup in ComponentSetup constructor
        ExtensionSetup extension = component.extension;

        // Creates a new extension instance
        Extension instance = extension.component.instance = model.newInstance(extension);
        VH_EXTENSION_CONFIGURATION.set(instance, extension); // sets Extension.configuration = extension setup

        // Invoke Extension#onNew()
        try {
            MH_EXTENSION_ON_NEW.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return extension;
    }
}

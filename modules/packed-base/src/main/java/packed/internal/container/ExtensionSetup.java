/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.container;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.BuildInfo;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;
import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.InternalExtensionException;
import app.packed.inject.Factory;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedWireletHandle;
import packed.internal.component.WireletWrapper;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** A setup class for an extension. Exposed to end-users as {@link ExtensionConfiguration}. */
public final class ExtensionSetup implements ExtensionConfiguration {

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

    /** A handle for accessing the field Extension#configuration, used by {@link #initialize(ContainerSetup, Class)}. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    /** The component setup of this extension. */
    private final ComponentSetup component;

    /** The setup of the container this extension belongs to. */
    private final ContainerSetup container;

    /** The extension instance, instantiated in {@link #initialize(ContainerSetup, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** A model of the extension. */
    private final ExtensionModel model;

    /**
     * Creates a new instance of this class as part of creating the extension component.
     * <p>
     * This constructor is called from the constructor of {@link ComponentSetup}.
     * 
     * @param component
     *            the component that this extension belongs to
     * @param model
     *            a model of the extension.
     */
    public ExtensionSetup(ComponentSetup component, ExtensionModel model) {
        this.component = requireNonNull(component);
        this.container = requireNonNull(component.getMemberOfContainer());
        this.model = requireNonNull(model);
    }

    /** {@inheritDoc} */
    @Override
    public BuildInfo build() {
        return component.build();
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
        if (container.children != null) {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the container this extension is a part of.
     * 
     * @return the container this extension is a part of
     */
    public ContainerSetup container() {
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension> extensionClass() {
        return model.extensionClass();
    }

    /**
     * Returns the extension instance.
     * 
     * @return the extension instance
     * @throws IllegalStateException
     *             if trying to call this method from the constructor of the extension
     */
    public Extension extensionInstance() {
        Extension e = instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of " + model.fullName());
        }
        return e;
    }

    Extension injectParent() {
        ContainerSetup parent = container.parent;
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
        return component.wire(ComponentDriver.driverInstall(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Factory<?> factory) {
        return component.wire(ComponentDriver.driverInstall(factory));
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration installInstance(Object instance) {
        return component.wire(ComponentDriver.driverInstallInstance(instance));
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

    /** {@inheritDoc} */
    @Override
    public Component link(Assembly<?> assembly, Wirelet... wirelets) {
        return component.link(assembly, wirelets);
    }

    /**
     * @param lookup
     */
    public void lookup(Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the extension's model.
     * 
     * @return the extension's model
     */
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

    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return component.path();
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

        // Finds the subtension's model and extension class
        SubtensionModel subModel = SubtensionModel.of(subtensionClass);
        Class<? extends Extension> subExtensionClass = subModel.extensionClass;

        // Check that requested subtension's extension is a direct dependency of this extension
        if (!model.dependencies().contains(subExtensionClass)) {
            // Special message if you try to use your own subtension
            if (model.extensionClass() == subExtensionClass) {
                throw new InternalExtensionException(model.extensionClass().getSimpleName() + " cannot use its own subtension "
                        + subExtensionClass.getSimpleName() + "." + subtensionClass.getSimpleName());
            }
            throw new InternalExtensionException(model.extensionClass().getSimpleName() + " must declare " + format(subModel.extensionClass)
                    + " as a dependency in order to use " + subExtensionClass.getSimpleName() + "." + subtensionClass.getSimpleName());
        }

        // Get the extension instance (create it if needed) thaw we need to create a subtension for
        Extension instance = container.useDependencyCheckedExtension(subExtensionClass, this).instance;

        // Create a new subtension instance using the extension instance and this.extensionClass as the requesting extension
        return (E) subModel.newInstance(instance, extensionClass());
    }

    /** {@inheritDoc} */
    @Override
    public <C extends ComponentConfiguration> C userWire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return container.component.wire(driver, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return component.wire(driver, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Wirelet> WireletHandle<T> wirelets(Class<T> wireletClass) {
        requireNonNull(wireletClass, "wireletClass is null");
        Module m = model.extensionClass().getModule();
        if (m != wireletClass.getModule()) {
            throw new InternalExtensionException("Must specify a wirelet that is in the same module (" + m.getName() + ") as '" + model.name()
                    + ", module of wirelet was " + wireletClass.getModule());
        }
        WireletWrapper wirelets = container.component.wirelets;
        if (wirelets == null) {
            return WireletHandle.of();
        }
        return new PackedWireletHandle<>(wirelets, wireletClass);
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
        ComponentSetup component = new ComponentSetup(container.component, model); // creates ExtensionSetup in ComponentSetup constructor
        ExtensionSetup extension = component.extension;

        // Creates a new extension instance
        Extension instance = extension.instance = model.newInstance(extension);
        VH_EXTENSION_CONFIGURATION.set(instance, extension); // sets Extension.configuration = extension setup

        // Invoke Extension#onNew()
        try {
            MH_EXTENSION_ON_NEW.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return extension;
    }

    public enum State {
        // lige nu har vi kun isConfigured...
        // Vi har brug for nogle flere states som minimum 3
        // Alt efter hvor vi lige lander med at lazy extension...
    }
}

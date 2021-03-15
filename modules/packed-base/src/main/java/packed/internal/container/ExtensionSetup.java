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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.BuildInfo;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.ExtensionConfiguration;
import app.packed.inject.Factory;
import packed.internal.component.ComponentSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** A setup class for an extension. Exposed to users as {@link ExtensionConfiguration}. */
public final class ExtensionSetup implements ExtensionConfiguration {

    /** A MethodHandle for invoking {@link Extension#extensionAdded()}. */
    private static final MethodHandle MH_EXTENSION_ADD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "extensionAdded", void.class);

    /** A MethodHandle for invoking {@link Extension#extensionConfigured()}. */
    private static final MethodHandle MH_EXTENSION_COMPLETE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "extensionConfigured",
            void.class);

    /** A MethodHandle for invoking {@link Extension#extensionBeforeDescendents()}. */
    private static final MethodHandle MH_EXTENSION_PRE_CHILD_CONTAINERS = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "extensionBeforeDescendents", void.class);

    /** A MethodHandle for invoking {@link #findWirelet(Class)} used by {@link ExtensionModel}. */
    static final MethodHandle MH_FIND_WIRELET = LookupUtil.lookupVirtual(MethodHandles.lookup(), "findWirelet", Object.class, Class.class);

    /** A VarHandle to access the field Extension#configuration, used by {@link #of(ContainerSetup, Class)}. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    /** The component setup of this extension. */
    private final ComponentSetup component;

    /** The setup of the container this extension belongs to. */
    private final ContainerSetup container;

    /** The extension instance, instantiated in {@link #of(ContainerSetup, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** A static model of the extension. */
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
    public void checkIsLeafBundle() {
        if (container.children != null) {
            throw new IllegalStateException();
        }
    }

    /** The extension is completed once the realm the container is part of is closed. */
    void complete() {
        try {
            MH_EXTENSION_COMPLETE.invoke(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        isConfigured = true;
    }
//
//    /** {@inheritDoc} */
//    @Override
//    public ConfigSite containerConfigSite() {
//        return bundle.compConf.configSite();
//    }

    /**
     * Returns the bundle this extension is a part of.
     * 
     * @return the bundle this extension is a part of
     */
    public ContainerSetup container() {
        return container;
    }

    /**
     * Returns the extension instance.
     * 
     * @return the extension instance
     * @throws IllegalStateException
     *             if trying to call this method from the constructor of the extension
     */
    public Extension extension() {
        Extension e = instance;
        if (e == null) {
            throw new IllegalStateException("Cannot call this method from the constructor of " + model.name());
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension> extensionClass() {
        return model.extensionClass();
    }

    /**
     * Used by {@link ExtensionModel}.
     * 
     * @param wireletType
     *            the type of wirelet
     * @return the wirelet or null
     */
    @Nullable
    Object findWirelet(Class<? extends Wirelet> wireletType) {
        return component.receiveWirelet(wireletType).orElse(null);
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
    public void link(Assembly<?> bundle, Wirelet... wirelets) {
        component.link(bundle, wirelets);
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

    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return component.path();
    }

    void preContainerChildren() {
        try {
            MH_EXTENSION_PRE_CHILD_CONTAINERS.invoke(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <E extends Subtension> E use(Class<E> subtensionClass) {
        requireNonNull(subtensionClass, "subtensionClass is null");

        // Find a model and extension class for the subtension
        SubtensionModel subModel = SubtensionModel.of(subtensionClass);
        Class<? extends Extension> subExtensionClass = subModel.extensionClass;

        // We need to check whether or not the extension is allowed to another extension (or any of its subtension) every time.
        // An alternative to explicit checks would be to cache the result of the check a map for each extension.
        // However this would incur extra memory usage. And if we only request an extension once
        // There would be significant overhead to instantiating a new map and caching the extension.
        // A better solution is that each extension caches the extensions they use (if they want to).
        // This saves a check + map lookup for each additional request.
        if (!model.dependencies().contains(subExtensionClass)) {
            // You cannot use your own subtensions
            if (model.extensionClass() == subExtensionClass) {
                throw new IllegalArgumentException("An extension cannot use its own subs " + model.extensionClass().getSimpleName() + ", subtension = "
                        + subtensionClass + ", valid dependencies = " + model.dependencies());
            } else {
                throw new UnsupportedOperationException("The specified extension type is not among the direct dependencies of "
                        + model.extensionClass().getSimpleName() + ", extensionClass = " + subtensionClass + ", valid dependencies = " + model.dependencies());
            }
        }

        // Get the extension instance (create it if needed) we are creating a subtension for
        Extension ext = container.useDependencyCheckedExtension(subExtensionClass, this).instance;

        // Create a new subtension instance using the extension and this.extensionClass as the requesting extension
        return (E) subModel.newInstance(ext, extensionClass());
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

    /**
     * Create and initialize a new extension.
     * 
     * @param container
     *            the setup of the container.
     * @param extensionClass
     *            the extension to initialize
     * @return a setup for the extension
     */
    static ExtensionSetup of(ContainerSetup container, Class<? extends Extension> extensionClass) {
        // Create setups and instantiate extension.
        ExtensionModel model = ExtensionModel.of(extensionClass);
        ComponentSetup component = new ComponentSetup(container.component, model); // creates ExtensionSetup in ComponentSetup constructor
        ExtensionSetup extension = component.extension;

        // Creates a new extension instance
        Extension ext = extension.instance = model.newInstance(extension);
        VH_EXTENSION_CONFIGURATION.set(ext, extension); // sets Extension.configuration = extension setup 

        // 1. The first step we take is seeing if there are parent or ancestors that needs to be notified
        // of the extensions existence. This is done first in order to let the remaining steps use any
        // information set by the parent or ancestor.
        if (model.mhExtensionLinked != null) {
            ExtensionSetup parentExtension = null;
            ContainerSetup parentContainer = container.parent;
            if (!model.extensionLinkedDirectChildrenOnly) {
                while (parentExtension == null && parentContainer != null) {
                    parentExtension = parentContainer.getExtensionContext(extensionClass);
                    parentContainer = parentContainer.parent;
                }
            } else if (parentContainer != null) {
                parentExtension = parentContainer.getExtensionContext(extensionClass);
            }

            // set activate extension???
            // If not just parent link keep checking up until root/
            if (parentExtension != null) {
                try {
                    model.mhExtensionLinked.invokeExact(parentExtension.instance, extension, ext);
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }
            }
        }

        // Invoke Extension#add() (should we run this before we link???)
        try {
            MH_EXTENSION_ADD.invoke(ext);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return extension;
    }
}

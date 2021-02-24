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
import packed.internal.component.ComponentBuild;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ExtensionConfiguration}. */
public final class PackedExtensionConfiguration implements ExtensionConfiguration, Comparable<PackedExtensionConfiguration> {

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

    /** A VarHandle used by {@link #of(PackedContainerConfiguration, Class)} to access the field Extension#configuration. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    /** The component configuration of this container. */
    private final ComponentBuild compConf;

    /** The container this extension is a part of. */
    private final PackedContainerConfiguration container;

    /** The actual instance of the extension, instantiated in {@link #of(PackedContainerConfiguration, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** A static model of the extension. */
    private final ExtensionModel model;

    /**
     * Creates a new instance of this class as part of creating the extension component.
     * 
     * @param compConf
     *            the component configuration that this extension belongs to
     * @param model
     *            a model of the extension.
     */
    public PackedExtensionConfiguration(ComponentBuild compConf, ExtensionModel model) {
        this.compConf = requireNonNull(compConf);
        this.container = requireNonNull(compConf.getMemberOfBundle());
        this.model = requireNonNull(model);
    }

    /** {@inheritDoc} */
    @Override
    public BuildInfo build() {
        return compConf.build();
    }

    /**
     * Returns the bundle this extension is a part of.
     * 
     * @return the bundle this extension is a part of
     */
    public PackedContainerConfiguration bundle() {
        return container;
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

    /** {@inheritDoc} */
    @Override
    public int compareTo(PackedExtensionConfiguration c) {
        return -model.compareTo(c.model);
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
     * Returns the extension instance this class wraps.
     * 
     * @return the extension instance this class wraps
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
    public Class<? extends Extension> extensionType() {
        return model.type();
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
        return compConf.receiveWirelet(wireletType).orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Class<?> implementation) {
        ComponentDriver<BaseComponentConfiguration> cd = BaseComponentConfiguration.driverInstall(implementation);
        return compConf.wire(cd);
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Factory<?> factory) {
        ComponentDriver<BaseComponentConfiguration> cd = BaseComponentConfiguration.driverInstall(factory);
        return compConf.wire(cd);
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration installInstance(Object instance) {
        ComponentDriver<BaseComponentConfiguration> cd = BaseComponentConfiguration.driverInstallInstance(instance);
        return compConf.wire(cd);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPartOfImage() {
        return container.isPartOfImage();
    }

    /** {@inheritDoc} */
    @Override
    public void link(Assembly<?> bundle, Wirelet... wirelets) {
        compConf.link(bundle, wirelets);
    }

    /**
     * @param lookup
     */
    public void lookup(Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the model of this extension.
     * 
     * @return the model of this extension
     */
    public ExtensionModel model() {
        return model;
    }

    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return compConf.path();
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
    public <E extends Subtension> E use(Class<E> subtensionType) {
        requireNonNull(subtensionType, "subtensionType is null");

        // Find a model for the subtension
        SubtensionModel sm = SubtensionModel.of(subtensionType);

        // We need to check whether or not the extension is allowed to use the specified extension every time.
        // An alternative would be to cache it in a map for each extension.
        // However this would incur extra memory usage. And if we only request an extension once
        // There would be significant overhead to instantiating a new map and caching the extension.
        // A better solution is that each extension caches the extensions they use (if they want to).
        // This saves a check + map lookup for each additional request.
        if (!model.isDirectDependency(sm.extensionType)) {
            // We allow an extension to use itself, alternative would be to throw an exception, but why?
            // You cannot use your own subs
            if (model.type() == sm.extensionType) {
                throw new IllegalArgumentException("An extension cannot use their own subs " + model.type().getSimpleName()
                        + ", extensionType = " + subtensionType + ", valid dependencies = " + model.dependencies());
            } else {
                throw new UnsupportedOperationException("The specified extension type is not among the direct dependencies of " + model.type().getSimpleName()
                        + ", extensionType = " + subtensionType + ", valid dependencies = " + model.dependencies());
            }
        }
        // Get the extension instance, creating it if needed
        Extension e = container.useExtension(sm.extensionType, this).instance;
        
        // Create a new Subtension instance using the extension and this.extensionType as the requesting extension
        return (E) sm.newInstance(e, model.type());
    }

    /** {@inheritDoc} */
    @Override
    public <C extends ComponentConfiguration> C userWire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return compConf.wire(driver, wirelets);
    }

    /**
     * Create and initialize a new extension.
     * 
     * @param container
     *            the configuration of the container.
     * @param extensionType
     *            the type of extension to initialize
     * @return the assembly of the extension
     */
    static PackedExtensionConfiguration of(PackedContainerConfiguration container, Class<? extends Extension> extensionType) {
        // Create extension context and instantiate extension
        ExtensionModel model = ExtensionModel.of(extensionType);
        ComponentBuild compConf = new ComponentBuild(container.compConf, model);
        PackedExtensionConfiguration assembly = compConf.extension;

        // Creates a new extension instance and set extension.configuration = assembly
        Extension extension = assembly.instance = model.newInstance(assembly);
        VH_EXTENSION_CONFIGURATION.set(extension, assembly);

        // 1. The first step we take is seeing if there are parent or ancestors that needs to be notified
        // of the extensions existence. This is done first in order to let the remaining steps use any
        // information set by the parent or ancestor.
        if (model.mhExtensionLinked != null) {
            PackedExtensionConfiguration parentExtension = null;
            PackedContainerConfiguration parent = container.parent;
            if (!model.extensionLinkedDirectChildrenOnly) {
                while (parentExtension == null && parent != null) {
                    parentExtension = parent.getExtensionContext(extensionType);
                    parent = parent.parent;
                }
            } else if (parent != null) {
                parentExtension = parent.getExtensionContext(extensionType);
            }

            // set activate extension???
            // If not just parent link keep checking up until root/
            if (parentExtension != null) {
                try {
                    model.mhExtensionLinked.invokeExact(parentExtension.instance, assembly, extension);
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }
            }
        }

        // Invoke Extension#add() (should we run this before we link???)
        try {
            MH_EXTENSION_ADD.invoke(extension);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return assembly;
    }
}

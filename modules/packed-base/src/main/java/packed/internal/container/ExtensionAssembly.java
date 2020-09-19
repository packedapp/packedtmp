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
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.base.TreePath;
import app.packed.component.AssemblyContext;
import app.packed.component.BeanConfiguration;
import app.packed.component.Bundle;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.ExtensionSetup;
import app.packed.inject.Factory;
import app.packed.statemachine.LifecycleContext;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.lifecycle.old.LifecycleContextHelper;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ExtensionConfiguration}. */
public final class ExtensionAssembly implements ExtensionConfiguration, Comparable<ExtensionAssembly> {

    /** A MethodHandle for invoking {@link Extension#add()}. */
    private static final MethodHandle MH_EXTENSION_ADD = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), Extension.class, "add", void.class);

    /** A MethodHandle for invoking {@link Extension#add()}. */
    static final MethodHandle MH_EXTENSION_PRE_CHILD_CONTAINERS = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), Extension.class, "preChildContainers",
            void.class);

    /** A MethodHandle for invoking {@link Extension#add()}. */
    static final MethodHandle MH_EXTENSION_COMPLETE = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), Extension.class, "complete", void.class);

    /** A MethodHandle for invoking {@link #findWirelet(Class)} used by {@link ExtensionModel}. */
    static final MethodHandle MH_FIND_WIRELET = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "findWirelet", Object.class, Class.class);

    /** A MethodHandle for invoking {@link #lifecycle()} used by {@link ExtensionModel}. */
    static final MethodHandle MH_LIFECYCLE_CONTEXT = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "lifecycle", LifecycleContext.class);

    /** A VarHandle used by {@link #of(ContainerAssembly, Class)} to access the field Extension#configuration. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.vhPrivateOther(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    /** This extension's component configuration. */
    private final ComponentNodeConfiguration compConf;

    /** The container this extension belongs to. */
    private final ContainerAssembly container;

    /** The extension instance this assembly wraps, instantiated in {@link #of(ContainerAssembly, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    @Nullable
    private Boolean isImage;

    /** A model of the extension. */
    private final ExtensionModel model;

    /**
     * Creates a new extension assembly.
     * 
     * @param compConf
     *            the component configuration that this extension belongs to
     * @param model
     *            a model of the extension.
     */
    public ExtensionAssembly(ComponentNodeConfiguration compConf, ExtensionModel model) {
        this.compConf = requireNonNull(compConf);
        this.container = requireNonNull(compConf.getMemberOfContainer());
        this.model = requireNonNull(model);
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyContext assembly() {
        return compConf.assembly();
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {

        // TODO FIXIXIXI

        // TODO, jeg syntes det skal vaere component vi henviser til...
//        if (container.containerState != 0) {
//            throw new IllegalStateException("This extension (" + instance().getClass().getSimpleName() + ") is no longer configurable");
//        }
//        if (isConfigured) {
//            throw new IllegalStateException("This extension (" + instance().getClass().getSimpleName() + ") is no longer configurable");
//        }
    }

    private void checkState(String expected) {
        String current = lifecycle().current();
        if (!current.equals(expected)) {
            throw new IllegalStateException("Expected " + expected + ", was " + current);
        }

    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ExtensionAssembly c) {
        return -model.compareTo(c.model);
    }

    /**
     * Returns the configuration of the container the extension is registered in.
     * 
     * @return the configuration of the container the extension is registered in
     */
    public ContainerAssembly container() {
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite containerConfigSite() {
        return container.compConf.configSite();
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
    public <T> BeanConfiguration<T> install(Class<T> implementation) {
        ComponentDriver<BeanConfiguration<T>> cd = BeanConfiguration.driver(implementation);
        return compConf.wire(cd);
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanConfiguration<T> install(Factory<T> factory) {
        ComponentDriver<BeanConfiguration<T>> cd = BeanConfiguration.driver(factory);
        return compConf.wire(cd);
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanConfiguration<T> installInstance(T instance) {
        ComponentDriver<BeanConfiguration<T>> cd = BeanConfiguration.driverInstance(instance);
        return compConf.wire(cd);
    }

    /**
     * Returns the extension instance this configuration wraps.
     * 
     * @return the extension instance this configuration wraps
     * @throws IllegalStateException
     *             if trying to call this method from the constructor of the extension
     */
    public Extension instance() {
        Extension e = instance;
        if (e == null) {
            throw new IllegalStateException("Cannot call this method from the constructor of " + model.nameSimple);
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInImage() {
        Boolean b = isImage;
        if (b != null) {
            return b;
        }
        ComponentNodeConfiguration cc = compConf.getParent();
        while (cc != null) {
            if (cc.modifiers().isImage()) {
                return isImage = Boolean.TRUE;
            }
            cc = cc.getParent();
        }
        return isImage = Boolean.FALSE;
    }

    /**
     * Returns a lifecycle context for the extension. Used by {@link #MH_LIFECYCLE_CONTEXT}.
     * 
     * @return a lifecycle context for the extension
     */
    private LifecycleContext lifecycle() {
        return new LifecycleContextHelper.SimpleLifecycleContext(ExtensionModel.Builder.STM.ld) {

            @Override
            protected int state() {
                if (instance == null) {
                    return 0;
                } else if (isConfigured == false) {
                    return 1;
                } else {
                    return 2;
                }
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        compConf.link(bundle, wirelets);
    }

    public ExtensionModel model() {
        return model;
    }

    void preContainerChildren() {
        try {
            MH_EXTENSION_PRE_CHILD_CONTAINERS.invoke(instance);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    void completed() {
        try {
            MH_EXTENSION_COMPLETE.invoke(instance);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    void onConfigured() {
        checkState(ExtensionSetup.NORMAL_USAGE);
        // model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_1_MAIN, instance, this);
        isConfigured = true;
        checkState(ExtensionSetup.CHILD_LINKING);
    }

    /** {@inheritDoc} */
    @Override
    public TreePath path() {
        return compConf.path();
    }

    /** {@inheritDoc} */
    @Override
    public <E extends Subtension> E use(Class<E> extensionType) {
        requireNonNull(extensionType, "extensionType is null");

        // This check is done in a class value
        // @SuppressWarnings("unchecked")
        // Class<? extends Extension> declaringClass = (Class<? extends Extension>) extensionType.getDeclaringClass();
        // Need to find injection
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Extension> T useOld(Class<T> extensionType) {
        // TODO can we call this method from the constructor????
        requireNonNull(extensionType, "extensionType is null");
        // We need to check whether or not the extension is allowed to use the specified extension every time.
        // An alternative would be to cache it in a map for each extension.
        // However this would incur extra memory usage. And if we only request an extension once
        // There would be significant overhead to instantiating a new map and caching the extension.
        // A better solution is that each extension caches the extensions they use (if they want to).
        // This saves a check + map lookup for each additional request.

        // We can use a simple bitmap here as well... But we need to move this method to PEC.
        // And then look up the context before we can check.

        if (!model.dependencies().contains(extensionType)) {
            // We allow an extension to use itself, alternative would be to throw an exception, but for what reason?
            if (extensionType == instance().getClass()) { // extension() checks for constructor
                return (T) instance;
            }

            throw new UnsupportedOperationException("The specified extension type is not among the dependencies of " + model.type().getSimpleName()
                    + ", extensionType = " + extensionType + ", valid dependencies = " + model.dependencies());
        }
        return (T) container.useExtension(extensionType, this).instance;
    }

    /** {@inheritDoc} */
    @Override
    public <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return compConf.wire(driver, wirelets);
    }

    /**
     * Creates and initializes a new extension and its context.
     * 
     * @param container
     *            the configuration of the container.
     * @param extensionType
     *            the type of extension to initialize
     * @return the new extension context
     */
    static ExtensionAssembly of(ContainerAssembly container, Class<? extends Extension> extensionType) {
        // Create extension context and instantiate extension
        ExtensionModel model = ExtensionModel.of(extensionType);
        ComponentNodeConfiguration compConf = new ComponentNodeConfiguration(container.compConf, model);
        ExtensionAssembly ea = compConf.extension;

        ea.checkState(ExtensionSetup.INSTANTIATING);
        Extension e = ea.instance = model.newInstance(ea); // Creates a new instance of the extension
        ea.checkState(ExtensionSetup.NORMAL_USAGE);

        // Set app.packed.container.Extension.configuration = ea
        VH_EXTENSION_CONFIGURATION.set(e, ea); // field is package-private in a public package

        // Run the following 3 steps before the extension is handed back to the user.
        // PackedExtensionConfiguration existing = container.activeExtension;
        try {
            // 1. The first step we take is seeing if there are parent or ancestors that needs to be notified
            // of the extensions existence. This is done first in order to let the remaining steps use any
            // information set by the parent or ancestor.

            // Should we also set the active extension in the parent???
            if (model.extensionLinkedToAncestorExtension != null) {
                ExtensionAssembly parentExtension = null;
                ContainerAssembly parent = container.parent;
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
                        model.extensionLinkedToAncestorExtension.invokeExact(parentExtension.instance, ea, e);
                    } catch (Throwable e1) {
                        throw ThrowableUtil.orUndeclared(e1);
                    }
                }
            }

            // Invoke Extension#add() (should we run this before we link???)
            try {
                MH_EXTENSION_ADD.invoke(e);
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
            }

        } finally {
            // container.activeExtension = existing;
        }
        return ea; // Return extension to users
    }

    /** {@inheritDoc} */
    @Override
    public void checkNoChildContainers() {
        container.checkNoChildContainers();
    }
}

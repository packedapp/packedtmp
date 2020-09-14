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
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.base.TreePath;
import app.packed.component.AssemblyContext;
import app.packed.component.BeanConfiguration;
import app.packed.component.Bundle;
import app.packed.component.ClassComponentDriver;
import app.packed.component.FactoryComponentDriver;
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

    /** A MethodHandle for invoking {@link #lifecycle()} used by {@link ExtensionModel}. */
    private static final MethodHandle MH_EXTENSION_ADDED = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), Extension.class, "add", void.class);

    /** A MethodHandle for invoking {@link #findWirelet(Class)} used by {@link ExtensionModel}. */
    static final MethodHandle MH_FIND_WIRELET = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "findWirelet", Object.class, Class.class);

    /** A MethodHandle for invoking {@link #lifecycle()} used by {@link ExtensionModel}. */
    static final MethodHandle MH_LIFECYCLE_CONTEXT = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "lifecycle", LifecycleContext.class);

    /** A VarHandle used by {@link #of(ContainerAssembly, Class)} to access the field Extension#configuration. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.vhPrivateOther(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    /** The container this extension is a part of */
    private final ContainerAssembly container;

    /** The extension instance this assembly wraps, instantiated in {@link #of(ContainerAssembly, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** A model of the extension. */
    private final ExtensionModel model;

    /** The component configuration of this extension. */
    private final ComponentNodeConfiguration compConf;

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
        this.container = requireNonNull(compConf.container());
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
        // TODO, jeg syntes det skal vaere component vi henviser til...
        if (container.containerState != 0) {
            throw new IllegalStateException("This extension (" + instance().getClass().getSimpleName() + ") is no longer configurable");
        }
        if (isConfigured) {
            throw new IllegalStateException("This extension (" + instance().getClass().getSimpleName() + ") is no longer configurable");
        }
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
        return container.compConf.receiveWirelet(wireletType).orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanConfiguration<T> install(Factory<T> factory) {
        return container.compConf.wire(BeanConfiguration.driver(), factory);
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanConfiguration<T> installInstance(T instance) {
        return container.compConf.wireInstance(BeanConfiguration.driver(), instance);
    }

    @Override
    public <C, I> C wire(ClassComponentDriver<C, I> driver, Class<? extends I> implementation, Wirelet... wirelets) {
        return container.compConf.wire(driver, implementation);
    }

    @Override
    public <C, I> C wire(FactoryComponentDriver<C, I> driver, Factory<? extends I> implementation, Wirelet... wirelets) {
        return container.compConf.wire(driver.bindToFactory(compConf.realm(), implementation), wirelets);
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

    /** Invoked by the container configuration, whenever the extension is configured. */
    void onChildrenConfigured() {
        checkState(ExtensionSetup.CHILD_LINKING);
        model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_2_CHILDREN_DONE, instance, this);
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    void onConfigured() {
        checkState(ExtensionSetup.NORMAL_USAGE);
        model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_1_MAIN, instance, this);
        isConfigured = true;
        checkState(ExtensionSetup.CHILD_LINKING);
    }

    /**
     * Returns an optional representing this extension. This is mainly to avoid allocation, as we can have a lot of them
     * 
     * @return an optional representing this extension
     */
    public Optional<Class<? extends Extension>> optional() {
        return model.optional;
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

            throw new UnsupportedOperationException("The specified extension type is not among " + model.type().getSimpleName()
                    + " dependencies, extensionType = " + extensionType + ", valid dependencies = " + model.dependencies());
        }
        return (T) container.useExtension(extensionType, this).instance;
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
        PackedRealm realm = PackedRealm.fromExtension(model.type());
        ComponentNodeConfiguration compConf = container.compConf.newChild(model.driver, container.compConf.configSite(), realm, null);

        ExtensionAssembly ea = compConf.extension;
        ea.checkState(ExtensionSetup.INSTANTIATING);
        Extension e = ea.instance = model.newInstance(ea); // Creates a new XXExtension instance
        ea.checkState(ExtensionSetup.NORMAL_USAGE);

        // Sets Extension.configuration = pec
        VH_EXTENSION_CONFIGURATION.set(e, ea); // field is package-private in a public package

        // Add a component configuration node

        // Run the following 3 steps before the extension is handed back to the user.
        // PackedExtensionConfiguration existing = container.activeExtension;
        try {
            // 1. The first step we take is seeing if there are parent or ancestors that needs to be notified
            // of the extensions existence. This is done first in order to let the remaining steps use any
            // information set by the parent or ancestor.

            // Should we also set the active extension in the parent???
            if (model.extensionLinkedToAncestorExtension != null) {
                ExtensionAssembly parentExtension = null;
                ContainerAssembly parent = container.compConf.container();
                if (!model.extensionLinkedDirectChildrenOnly) {
                    while (parentExtension == null && parent != null) {
                        parentExtension = parent.getExtensionContext(extensionType);
                        parent = parent.compConf.container();
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

            // Invoke Extension#added() (should we run this before we link???)
            try {
                MH_EXTENSION_ADDED.invoke(e);
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
            }

            // 2. Invoke all methods on the extension annotated with @When(Normal)
            model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_0_INSTANTIATION, e, ea);

            // 3. Finally initialize any pipeline (??swap step 2 and 3??)
//            if (container.node.wirelets != null) {
//                container.node.wirelets.extensionInitialized(pec);
//            }
        } finally {
            // container.activeExtension = existing;
        }
        return ea; // Return extension to users
    }
}

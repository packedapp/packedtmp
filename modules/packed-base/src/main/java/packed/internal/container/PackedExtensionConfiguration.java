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
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.base.Contract;
import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.ComponentPath;
import app.packed.component.SingletonConfiguration;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerDescriptor;
import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.ExtensionSidecar;
import app.packed.inject.Factory;
import app.packed.lifecycle.LifecycleContext;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.lifecycle.LifecycleContextHelper;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ExtensionConfiguration}. */
public final class PackedExtensionConfiguration implements ExtensionConfiguration, Comparable<PackedExtensionConfiguration> {

    /** A MethodHandle for invoking {@link #findWirelet(Class)} used by {@link ExtensionModel}. */
    static final MethodHandle MH_FIND_WIRELET = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "findWirelet", Object.class, Class.class);

    /** A MethodHandle for invoking {@link #lifecycle()} used by {@link ExtensionModel}. */
    static final MethodHandle MH_LIFECYCLE_CONTEXT = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "lifecycle", LifecycleContext.class);

    /** A MethodHandle for invoking {@link #lifecycle()} used by {@link ExtensionModel}. */
    private static final MethodHandle MH_EXTENSION_ADDED = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), Extension.class, "added", void.class);

    /** A VarHandle used by {@link #of(PackedContainerRole, Class)} to access the field Extension#configuration. */
    private static final VarHandle VH_EXTENSION_CONFIGURATION = LookupUtil.vhPrivateOther(MethodHandles.lookup(), Extension.class, "configuration",
            ExtensionConfiguration.class);

    /** The extension instance this configuration wraps, initialized in {@link #of(PackedContainerRole, Class)}. */
    @Nullable
    private Extension instance;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** The sidecar model of the extension. */
    private final ExtensionModel model;

    /** The container this extension belongs to. */
    private final PackedContainerRole container;

    /** The realm of this extension. */
    private final PackedRealm realm;

    /** The component node of the extension. */
    final ComponentNodeConfiguration node;

    /**
     * Creates a new configuration.
     * 
     * @param container
     *            the configuration of the container that uses the extension
     * @param model
     *            a model of the extension.
     */
    private PackedExtensionConfiguration(PackedContainerRole container, ExtensionModel model) {
        this.container = requireNonNull(container);
        this.model = requireNonNull(model);
        this.realm = PackedRealm.fromExtension(this);
        this.node = container.node.newChild(model.driver(), container.node.configSite(), realm, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    void buildDescriptor(ContainerDescriptor.Builder builder) {
        MethodHandle mha = model.bundleBuilderMethod;
        if (mha != null) {
            try {
                mha.invoke(instance, builder);
            } catch (Throwable e1) {
                throw new UndeclaredThrowableException(e1);
            }
        }

        for (Object s : model.contracts().values()) {
            // TODO need a context
            Contract con;
            if (s instanceof Function) {
                con = (Contract) ((Function) s).apply(instance);
            } else if (s instanceof BiFunction) {
                con = (Contract) ((BiFunction) s).apply(instance, null);
            } else {
                // MethodHandle...
                try {
                    MethodHandle mh = (MethodHandle) s;
                    if (mh.type().parameterCount() == 0) {
                        con = (Contract) mh.invoke(instance);
                    } else {
                        con = (Contract) mh.invoke(instance);
                    }
                } catch (Throwable e1) {
                    throw new UndeclaredThrowableException(e1);
                }
            }
            requireNonNull(con);
            builder.addContract(con);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
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
    public int compareTo(PackedExtensionConfiguration c) {
        return -model.compareTo(c.model);
    }

    /**
     * Returns the configuration of the container the extension is registered in.
     * 
     * @return the configuration of the container the extension is registered in
     */
    public PackedContainerRole container() {
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite containerConfigSite() {
        return container.node.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension> extensionType() {
        return model.extensionType();
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
        return container.node.receiveWirelet(wireletType).orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        return container.node.wire(SingletonConfiguration.driver(), factory);
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> installInstance(T instance) {
        return container.node.wireInstance(SingletonConfiguration.driver(), instance);
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
            throw new IllegalStateException("Cannot call this method from the constructor of " + model.type().getSimpleName());
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
        node.link(bundle, wirelets);
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    void onChildrenConfigured() {
        checkState(ExtensionSidecar.CHILD_LINKING);
        model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_2_CHILDREN_DONE, instance, this);
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    void onConfigured() {
        checkState(ExtensionSidecar.NORMAL_USAGE);
        model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_1_MAIN, instance, this);
        isConfigured = true;
        checkState(ExtensionSidecar.CHILD_LINKING);
    }

    /**
     * Returns an optional representing this extension. This is mainly to avoid allocation, as we can have a lot of them
     * 
     * @return an optional representing this extension
     */
    public Optional<Class<? extends Extension>> optional() {
        return model.optional;
    }

    /**
     * Returns the realm this extension belongs to.
     * 
     * @return the realm this extension belongs to
     */
    public PackedRealm realm() {
        return realm;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        // TODO return path of this component.
        return node.path();
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

        if (!model.directDependencies().contains(extensionType)) {
            // We allow an extension to use itself, alternative would be to throw an exception, but for what reason?
            if (extensionType == instance().getClass()) { // extension() checks for constructor
                return (T) instance;
            }

            throw new UnsupportedOperationException("The specified extension type is not among " + model.extensionType().getSimpleName()
                    + " dependencies, extensionType = " + extensionType + ", valid dependencies = " + model.directDependencies());
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
    static PackedExtensionConfiguration of(PackedContainerRole container, Class<? extends Extension> extensionType) {
        // I think move to the constructor of this context??? Then extension can be final...
        // Create extension context and instantiate extension
        ExtensionModel model = ExtensionModel.of(extensionType);
        PackedExtensionConfiguration pec = new PackedExtensionConfiguration(container, model);
        pec.checkState(ExtensionSidecar.INSTANTIATING);
        Extension e = pec.instance = model.newInstance(pec); // Creates a new XXExtension instance
        pec.checkState(ExtensionSidecar.NORMAL_USAGE);

        // Sets Extension.configuration = pec
        VH_EXTENSION_CONFIGURATION.set(e, pec); // field is package-private in a public package

        // Add a component configuration node

        // Run the following 3 steps before the extension is handed back to the user.
        // PackedExtensionConfiguration existing = container.activeExtension;
        try {
            // 1. The first step we take is seeing if there are parent or ancestors that needs to be notified
            // of the extensions existence. This is done first in order to let the remaining steps use any
            // information set by the parent or ancestor.

            // Should we also set the active extension in the parent???
            if (model.extensionLinkedToAncestorExtension != null) {
                PackedExtensionConfiguration parentExtension = null;
                PackedContainerRole parent = container.node.container();
                if (!model.extensionLinkedDirectChildrenOnly) {
                    while (parentExtension == null && parent != null) {
                        parentExtension = parent.getContext(extensionType);
                        parent = parent.node.container();
                    }
                } else if (parent != null) {
                    parentExtension = parent.getContext(extensionType);
                }

                // set activate extension???
                // If not just parent link keep checking up until root/
                if (parentExtension != null) {
                    try {
                        model.extensionLinkedToAncestorExtension.invokeExact(parentExtension.instance, pec, e);
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
            model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_0_INSTANTIATION, e, pec);

            // 3. Finally initialize any pipeline (??swap step 2 and 3??)
//            if (container.node.wirelets != null) {
//                container.node.wirelets.extensionInitialized(pec);
//            }
        } finally {
            // container.activeExtension = existing;
        }
        return pec; // Return extension to users
    }

    /** {@inheritDoc} */
    @Override
    public <E extends Subtension> E use(Class<E> extensionType) {
        requireNonNull(extensionType, "extensionType is null");

        // This check is done in a class value
        @SuppressWarnings("unchecked")
        Class<? extends Extension> declaringClass = (Class<? extends Extension>) extensionType.getDeclaringClass();
        System.out.println(declaringClass);
        // Need to find injection
        throw new UnsupportedOperationException();
    }
}

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
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.analysis.BundleDescriptor;
import app.packed.base.Contract;
import app.packed.base.Nullable;
import app.packed.component.ComponentPath;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import app.packed.lifecycle.LifecycleContext;
import packed.internal.lifecycle.LifecycleContextHelper;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** The default implementation of {@link ExtensionContext} with addition methods only available in app.packed.base. */
public final class PackedExtensionContext implements ExtensionContext, Comparable<PackedExtensionContext> {

    // Indicates that a bundle has already been configured...
    public static final ExtensionContext CONFIGURED = new PackedExtensionContext();

    static final MethodHandle MH_FIND_WIRELET = LookupUtil.findVirtualEIIE(MethodHandles.lookup(), "findWirelet",
            MethodType.methodType(Object.class, Class.class));

    /** A MethodHandle for {@link #lifecycle()}. */
    public static final MethodHandle MH_LIFECYCLE_CONTEXT = LookupUtil.findVirtualEIIE(MethodHandles.lookup(), "lifecycle",
            MethodType.methodType(LifecycleContext.class));

    /** The extension instance this context wraps, initialized in {@link #of(PackedContainerConfiguration, Class)}. */
    @Nullable
    private Extension extension;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** The sidecar model of the extension. */
    private final ExtensionModel model;

    /** The configuration of the container the extension is registered in. */
    private final PackedContainerConfiguration pcc;

    private PackedExtensionContext() {
        this.pcc = null;
        this.model = null;
    }

    /**
     * Creates a new extension context.
     * 
     * @param pcc
     *            the configuration of the container the extension is registered in
     * @param model
     *            a model of the extension.
     */
    private PackedExtensionContext(PackedContainerConfiguration pcc, ExtensionModel model) {
        this.pcc = requireNonNull(pcc);
        this.model = requireNonNull(model);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    void buildDescriptor(BundleDescriptor.Builder builder) {
        MethodHandle mha = model.bundleBuilderMethod;
        if (mha != null) {
            try {
                mha.invoke(extension, builder);
            } catch (Throwable e1) {
                throw new UndeclaredThrowableException(e1);
            }
        }

        for (Object s : model.contracts().values()) {
            // TODO need a context
            Contract con;
            if (s instanceof Function) {
                con = (Contract) ((Function) s).apply(extension);
            } else if (s instanceof BiFunction) {
                con = (Contract) ((BiFunction) s).apply(extension, null);
            } else {
                // MethodHandle...
                try {
                    MethodHandle mh = (MethodHandle) s;
                    if (mh.type().parameterCount() == 0) {
                        con = (Contract) mh.invoke(extension);
                    } else {
                        con = (Contract) mh.invoke(extension, null);
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
        if (isConfigured) {
            throw new IllegalStateException("This extension (" + extension().getClass().getSimpleName() + ") is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(PackedExtensionContext c) {
        return -model.compareTo(c.model);
    }

    /**
     * Returns the configuration of the container the extension is registered in.
     * 
     * @return the configuration of the container the extension is registered in
     */
    public PackedContainerConfiguration container() {
        return pcc;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite containerConfigSite() {
        return pcc.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath containerPath() {
        return pcc.path();
    }

    /**
     * Returns the extension instance this context wraps.
     * 
     * @return the extension instance this context wraps
     * @throws IllegalStateException
     *             if trying to call this method from the constructor of the extension
     */
    public Extension extension() {
        Extension e = extension;
        if (e == null) {
            throw new IllegalStateException("Cannot call this method from the constructor of the extension");
        }
        return e;
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
        return container().wireletAny(wireletType).orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        return pcc.install(factory);
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> installInstance(T instance) {
        return pcc.installInstance(instance);
    }

    LifecycleContext lifecycle() {
        return new LifecycleContextHelper.SimpleLifecycleContext(ExtensionModel.Builder.STM.ld) {

            @Override
            protected int state() {
                return extension == null ? 0 : 1;
            }
        };
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    public void onChildrenConfigured() {
        model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_2_CHILDREN_DONE, extension, this);
        isConfigured = true;
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    public void onConfigured() {
        model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_1_MAIN, extension, this);
        isConfigured = true;
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
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
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
            if (extensionType == extension().getClass()) { // extension() checks for constructor
                return (T) extension;
            }

            throw new UnsupportedOperationException("The specified extension type is not among " + model.extensionType().getSimpleName()
                    + " dependencies, extensionType = " + extensionType + ", valid dependencies = " + model.directDependencies());
        }
        return (T) pcc.useExtension(extensionType, this).extension;
    }

    /**
     * Creates and initializes a new extension and its context.
     * 
     * @param pcc
     *            the configuration of the container.
     * @param extensionType
     *            the type of extension to initialize
     * @return the new extension context
     */
    public static PackedExtensionContext of(PackedContainerConfiguration pcc, Class<? extends Extension> extensionType) {
        // Create extension context and instantiate extension
        ExtensionModel model = ExtensionModel.of(extensionType);
        PackedExtensionContext pec = new PackedExtensionContext(pcc, model);
        Extension e = pec.extension = model.newInstance(pec);
        ModuleAccess.container().extensionSetContext(e, pec);

        PackedExtensionContext existing = pcc.activeExtension;
        try {
            pcc.activeExtension = pec;
            model.invokePostSidecarAnnotatedMethods(ExtensionModel.ON_0_INSTANTIATION, e, pec);
            if (pcc.wireletContext != null) {
                pcc.wireletContext.extensionInitialized(pec);
            }

            // See if there are extensions of the same type in parent containers that needs to
            // notified of the addition of the extension. This must happen before the child extension
            // is returned to the user.

            // Should we also set the active extension in the parent???
            if (model.extensionLinkedToAncestorExtension != null) {
                PackedExtensionContext parentExtension = null;
                PackedContainerConfiguration parent = pcc.container();
                if (!model.extensionLinkedDirectChildrenOnly) {
                    while (parentExtension == null && parent != null) {
                        parentExtension = parent.getExtension(extensionType);
                        parent = parent.container();
                    }
                } else if (parent != null) {
                    parentExtension = parent.getExtension(extensionType);
                }

                // set activate extension???
                // If not just parent link keep checking up until root/
                if (parentExtension != null) {
                    try {
                        model.extensionLinkedToAncestorExtension.invokeExact(parentExtension.extension, pec, e);
                    } catch (Throwable e1) {
                        throw ThrowableUtil.easyThrow(e1);
                    }
                }
            }
        } finally {
            pcc.activeExtension = existing;
        }
        return pec;
    }
}

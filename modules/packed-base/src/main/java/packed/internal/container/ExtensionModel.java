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
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Contract;
import app.packed.base.Nullable;
import app.packed.base.OnAssembling;
import app.packed.component.Component;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.ExtensionSidecar;
import app.packed.container.WireletPipeline;
import app.packed.container.InternalExtensionException;
import app.packed.hook.Expose;
import app.packed.hook.OnHook;
import packed.internal.hook.BaseHookQualifierList;
import packed.internal.hook.OnHookModel;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.reflect.OpenClass;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/** A model of an Extension. */
public final class ExtensionModel<E extends Extension> {

    /** A cache of values. */
    private static final ClassValue<ExtensionModel<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionModel<? extends Extension> computeValue(Class<?> type) {
            // First, check that the user has specified an actual sub type of Extension to
            // ContainerConfiguration#use() or Bundle#use()
            if (type == Extension.class) {
                throw new IllegalArgumentException("Cannot specify " + Extension.class.getSimpleName() + ".class as a parameter");
            } else if (!Extension.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(
                        "The specified type '" + StringFormatter.format(type) + "' does not extend '" + StringFormatter.format(Extension.class) + "'");
            }
            return ExtensionModelLoader.load((Class<? extends Extension>) type);

        }
    };

    final MethodHandle bundleBuilderMethod;

    /** The method handle used to create a new extension instance. */
    private final MethodHandle constructor;

    /** It is important this map is immutable as the key set is exposed via ExtensionDescriptor. */
    // Can 2 extensions define the same contract???? Don't think so
    // If not we could have a Contract.class->ContractFactory Map and a Contract.of(ContainerSource, Class<T extends
    // Contract>);
    final Map<Class<? extends Contract>, MethodHandle> contracts;

    final Set<Class<? extends Extension>> dependenciesDirect;

    final List<Class<? extends Extension>> dependenciesTotalOrder;

    /** The type of the extension this model describes. */
    final Class<? extends Extension> extensionType;

    final List<ECall> l;

    final BaseHookQualifierList nonActivatingHooks;

    @Nullable
    private final OnHookModel onHookModel;

    /** An optional containing the extension type. To avoid excessive creation of them for {@link Component#extension()}. */
    public final Optional<Class<? extends Extension>> optional;

    final Map<Class<? extends WireletPipeline<?, ?, ?>>, ExtensionWireletPipelineModel> pipelines;

    /**
     * Creates a new extension model from the specified builder.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionModel(Builder builder) {
        this.constructor = builder.constructor;
        this.extensionType = builder.extensionType;
        this.pipelines = Map.copyOf(builder.pipelines);
        this.bundleBuilderMethod = builder.builderMethod;
        this.contracts = Map.copyOf(builder.contracts);
        this.dependenciesDirect = Set.copyOf(builder.dependenciesDirect);
        this.dependenciesTotalOrder = builder.dependenciesTotalOrder;
        this.optional = Optional.of(extensionType); // No need to create an optional every time we need this

        this.onHookModel = builder.onHookModel;
        this.nonActivatingHooks = onHookModel == null ? null : LazyExtensionActivationMap.findNonExtending(onHookModel);
        this.l = List.copyOf(builder.l);
    }

    /**
     * Creates a new instance of the extension.
     * 
     * @param context
     *            the extension context that can be constructor injected into the extension
     * @return a new instance of the extension
     */
    public E newExtensionInstance(PackedExtensionContext context) {
        // Time goes from around 1000 ns to 12 ns when we cache the method handle.
        // With LambdaMetafactory wrapped in a supplier we can get down to 6 ns
        try {
            if (constructor.type().parameterCount() > 0) {
                return (E) constructor.invoke(context);
            } else {
                return (E) constructor.invoke();
            }
        } catch (Throwable e) {
            ThrowableUtil.throwIfUnchecked(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Returns an extension model for the specified extension type.
     * 
     * @param <T>
     *            the type of extension to return a model for
     * @param extensionType
     *            the type of extension to return a model for
     * @return an extension model for the specified extension type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Extension> ExtensionModel<T> of(Class<T> extensionType) {
        return (ExtensionModel<T>) CACHE.get(extensionType);
    }

    /**
     * Returns a on hook model of all the methods annotated with {@link OnHook} on the extension. Or null if the extension
     * does not define any methods annotated with {@link OnHook}.
     * 
     * @param extensionType
     *            the extension type to return the model for
     * @return a hook model
     */
    @Nullable
    public static OnHookModel onHookModelOf(Class<? extends Extension> extensionType) {
        return of(extensionType).onHookModel;
    }

    /** A builder for {@link ExtensionModel}. */
    static final class Builder {

        MethodHandle builderMethod;

        /** The constructor used to create a new extension instance. */
        private MethodHandle constructor;

        // Need to check that a contract never belongs to two extension.
        // Also, I think we want to do this atomically, so that we do not have half an extension registered somewhere.
        // This means we want to synchronize things.
        // So add all shit, quick validation-> Sync->Validate final -> AddAll ->UnSync
        final IdentityHashMap<Class<? extends Contract>, MethodHandle> contracts = new IdentityHashMap<>();

        /** A list of dependencies on other extensions. */
        Set<Class<? extends Extension>> dependenciesDirect = new HashSet<>();

        private List<Class<? extends Extension>> dependenciesTotalOrder;

        /** The type of extension we are building a model for. */
        final Class<? extends Extension> extensionType;

        private final ArrayList<ECall> l = new ArrayList<>();

        final ExtensionModelLoader loader;

        /** A builder for all methods annotated with {@link OnHook} on the extension. */
        private OnHookModel onHookModel;

        final HashMap<Class<? extends WireletPipeline<?, ?, ?>>, ExtensionWireletPipelineModel> pipelines = new HashMap<>();

        /**
         * Creates a new builder.
         * 
         * @param extensionType
         *            the type of extension we are building a model for
         */
        Builder(Class<? extends Extension> extensionType, ExtensionModelLoader loader) {
            this.extensionType = requireNonNull(extensionType);
            this.loader = requireNonNull(loader);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ExtensionModel<?> build() {
            ExtensionSidecar em = extensionType.getAnnotation(ExtensionSidecar.class);
            if (em != null) {
                for (Class<? extends Extension> ccc : em.dependencies()) {
                    ExtensionModelLoader.load(ccc, loader);
                    dependenciesDirect.add(ccc);
                }
                for (Class<? extends WireletPipeline<?, ?, ?>> c : em.pipelines()) {
                    ExtensionWireletPipelineModel m = new ExtensionWireletPipelineModel.Builder(c).build();
                    pipelines.put(c, m);
                }
            }

            this.dependenciesTotalOrder = OldExtensionUseModel2.totalOrder(extensionType);

            OpenClass cp = new OpenClass(MethodHandles.lookup(), extensionType, true);
            this.constructor = ConstructorFinder.find(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            this.onHookModel = OnHookModel.newModel(cp, false, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY, ContainerConfiguration.class);
            cp.findMethods(e -> {

                OnAssembling oa = e.getAnnotation(OnAssembling.class);
                if (oa != null) {
                    if (Modifier.isStatic(e.getModifiers())) {
                        throw new InternalExtensionException("Methods annotated with " + OnAssembling.class + " cannot be static, method = " + e);
                    }
                    MethodHandle mh = cp.unreflect(e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    l.add(new ECall(mh, oa.value().equals(ExtensionSidecar.ON_INSTANTIATION), oa.value().equals(ExtensionSidecar.ON_PREEMBLE)));
                }
                Expose ex = e.getAnnotation(Expose.class);
                if (ex != null) {
                    if (e.getReturnType() == void.class) {
                        builderMethod = cp.unreflect(e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    } else {
                        MethodHandle mh = cp.unreflect(e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                        contracts.put((Class) e.getReturnType(), mh);
                    }
                }
            });
            return new ExtensionModel<>(this);
        }
    }

    static class ECall {
        final MethodHandle mh;
        final boolean onInstantiation;
        final boolean onMainFinished;

        ECall(MethodHandle mh, boolean onInstantiation, boolean onMainFinished) {
            this.mh = requireNonNull(mh);
            this.onInstantiation = onInstantiation;
            this.onMainFinished = onMainFinished;
        }
    }
}

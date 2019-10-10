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
package packed.internal.container.extension;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionComposer;
import app.packed.container.extension.ExtensionDeclarationException;
import app.packed.container.extension.ExtensionInstantiationContext;
import app.packed.container.extension.ExtensionIntrospectionContext;
import app.packed.container.extension.ExtensionWireletPipeline;
import app.packed.contract.Contract;
import packed.internal.hook.HGBModel;
import packed.internal.hook.HookClassBuilder;
import packed.internal.hook.OnHookGroupModel;
import packed.internal.module.ModuleAccess;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.reflect.MemberFinder;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;

/** A model of an Extension. */
public final class ExtensionModel<T extends Extension> {

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
            return new Builder((Class<? extends Extension>) type).build();
        }
    };

    /** The method handle used to create a new extension instance. */
    private final MethodHandle constructor;

    /** The type of the extension this model describes. */
    public final Class<? extends Extension> extensionType;

    final OnHookGroupModel hooks;

    final Map<Class<? extends ExtensionWireletPipeline<?, ?>>, Function<?, ?>> pipelines;

    /** It is important this map is immutable as the key set is exposed via ExtensionDescriptor. */
    // Can 2 extensions define the same contract???? Don't think so
    // If not we could have a Contract.class->ContractFactory Map and a Contract.of(ContainerSource, Class<T extends
    // Contract>);
    public final Map<Class<? extends Contract>, BiFunction<?, ? super ExtensionIntrospectionContext, ?>> contracts;

    public final BiConsumer<? super Extension, ? super app.packed.container.BundleDescriptor.Builder> bundleBuilder;

    public final Consumer<? super Extension> onAdd;

    public final Consumer<? super Extension> onConfigured;

    public final BiConsumer<? super Extension, ? super ExtensionInstantiationContext> onInstantiation;

    public final Set<HGBModel> groups;

    public final BiConsumer<? super Extension, ? super Extension> onLinkage;

    public final Set<Class<? extends Extension>> dependencies;

    public final Optional<Class<? extends Extension>> optional;

    /**
     * Creates a new extension model from the specified builder.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionModel(Builder builder) {
        this.constructor = builder.constructor;
        this.extensionType = builder.extensionType;
        this.hooks = new OnHookGroupModel(builder.hooks, extensionType);
        this.pipelines = Map.copyOf(builder.pipelines);
        this.bundleBuilder = builder.builder;
        this.contracts = Map.copyOf(builder.contracts);
        this.onAdd = builder.onExtensionInstantiatedAction;
        this.onConfigured = builder.onConfiguredAction;
        this.onInstantiation = builder.onInstantiation;
        this.groups = Set.copyOf(builder.hgbs);
        this.onLinkage = builder.onLinkage;
        this.dependencies = Set.copyOf(builder.dependencies);
        this.optional = Optional.of(extensionType);
        // this.optional = Optional.of(extensionType)
        // Saa slipper vi for at lave en ny optional hver gang....
    }

    public OnHookGroupModel hooks() {
        return hooks;
    }

    /**
     * Creates a new instance of the extension.
     * 
     * @return a new instance of the extension
     */
    public T newInstance() {
        // Time goes from around 1000 ns to 12 ns when we cache the method handle.
        // With LambdaMetafactory wrapped in a supplier we can get down to 6 ns
        try {
            return (T) constructor.invoke();
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
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

    /** A builder for {@link ExtensionModel}. This class is public in order to called from {@link ExtensionComposer}. */
    static final class Builder extends ExtensionComposerContext {

        /** The constructor used to create a new extension instance. */
        private final MethodHandle constructor;

        final Class<? extends Extension> extensionType;

        final HookClassBuilder hooks;

        @SuppressWarnings("unchecked")
        private Builder(Class<? extends Extension> extensionType) {
            this.extensionType = extensionType;
            // if (!Modifier.isFinal(extensionType.getModifiers())) {
            // throw new ExtensionDeclarationException("The extension '" + StringFormatter.format(extensionType) + "' must be
            // declared final");
            // }
            constructor = ConstructorFinder.find(extensionType);
            this.hooks = new HookClassBuilder(extensionType, false);

            Class<? extends ExtensionComposer<?>> composerType = null;

            for (Class<?> c : extensionType.getDeclaredClasses()) {
                if (c.getSimpleName().equals("Composer")) {
                    if (!ExtensionComposer.class.isAssignableFrom(c)) {
                        throw new ExtensionDeclarationException(c.getCanonicalName() + " must extend " + StringFormatter.format(ExtensionComposer.class));
                    }
                    composerType = (Class<? extends ExtensionComposer<?>>) c;
                }
            }

            if (composerType != null) {
                // composerType = (Class<? extends ExtensionComposer<?>>) COMPOSABLE_EXTENSION_TV_EXTRACTOR.extract(extensionType);
                // if (!Modifier.isFinal(nodeType.getModifiers())) {
                // throw new ExtensionDeclarationException(
                // "The extension node returned by onAdded(), must be declareda final, node type = " +
                // StringFormatter.format(nodeType));
                // }
                ExtensionComposer<?> ep = null;
                MethodHandle mh = ConstructorFinder.find(composerType);
                try {
                    ep = (ExtensionComposer<?>) mh.invoke();
                } catch (Throwable e) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e);
                    throw new UndeclaredThrowableException(e);
                }
                ModuleAccess.extension().configureComposer(ep, this);

                for (HGBModel g : hgbs) {
                    hooks.onHookGroup(g);
                }
            }
        }

        private ExtensionModel<?> build() {
            MemberFinder.findMethods(Extension.class, extensionType, method -> hooks.onHook(method));
            return new ExtensionModel<>(this);
        }
    }
}
// Raekkefoelge af installeret extensions....
// Maaske bliver vi noedt til at have @UsesExtension..
// Saa vi kan sige X extension skal koeres foerend Y extension

// Configure
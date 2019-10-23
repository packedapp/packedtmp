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
import java.lang.invoke.MethodHandles;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import app.packed.api.Contract;
import app.packed.component.Component;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.ExtensionComposer;
import app.packed.container.ExtensionDescriptorContext;
import app.packed.container.ExtensionInstantiationContext;
import app.packed.container.ExtensionWirelet;
import app.packed.container.InternalExtensionException;
import app.packed.hook.OnHook;
import packed.internal.hook.model.OnHookContainerModel;
import packed.internal.hook.model.OnHookContainerModelBuilder;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.reflect.ConstructorFinder;
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

    public final BiConsumer<? super Extension, ? super app.packed.container.BundleDescriptor.Builder> bundleBuilder;

    /** The method handle used to create a new extension instance. */
    private final MethodHandle constructor;

    /** It is important this map is immutable as the key set is exposed via ExtensionDescriptor. */
    // Can 2 extensions define the same contract???? Don't think so
    // If not we could have a Contract.class->ContractFactory Map and a Contract.of(ContainerSource, Class<T extends
    // Contract>);
    public final Map<Class<? extends Contract>, BiFunction<?, ? super ExtensionDescriptorContext, ?>> contracts;

    public final Set<Class<? extends Extension>> dependencies;

    /** The type of the extension this model describes. */
    public final Class<? extends Extension> extensionType;

    public final Consumer<? super Extension> onAdd;

    public final Consumer<? super Extension> onConfigured;

    public final BiConsumer<? super Extension, ? super ExtensionInstantiationContext> onInstantiation;

    public final BiConsumer<? super Extension, ? super Extension> onLinkage;

    /** An optional containing the extension type. To avoid excessive creation of them for {@link Component#extension()}. */
    public final Optional<Class<? extends Extension>> optional;

    public final Map<Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>>, BiFunction<?, ?, ?>> pipelines;

    public final OnHookContainerModel hooks;

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
        this.bundleBuilder = builder.builder;
        this.contracts = Map.copyOf(builder.contracts);
        this.onAdd = builder.onExtensionInstantiatedAction;
        this.onConfigured = builder.onConfiguredAction;
        this.onInstantiation = builder.onInstantiation;
        this.onLinkage = builder.onLinkage;
        this.dependencies = Set.copyOf(builder.dependencies);
        this.optional = Optional.of(extensionType); // No need to create an optional every time we need this

        hooks = builder.hooks.build();
    }

    /**
     * Returns a model of all the methods annotated with {@link OnHook} on the extension.
     * 
     * @return a hook model
     */
    public OnHookContainerModel hooks() {
        return hooks;
    }

    /**
     * Creates a new instance of the extension.
     * 
     * @return a new instance of the extension
     */
    public T newInstance(PackedExtensionContext context) {
        // Time goes from around 1000 ns to 12 ns when we cache the method handle.
        // With LambdaMetafactory wrapped in a supplier we can get down to 6 ns
        try {
            if (constructor.type().parameterCount() > 0) {
                return (T) constructor.invoke(context);
            } else {
                return (T) constructor.invoke();
            }
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

    /** A builder for {@link ExtensionModel}. */
    private static final class Builder extends AbstractExtensionModelBuilder {

        /** The constructor used to create a new extension instance. */
        private final MethodHandle constructor;

        /** The type of extension we are building a model for. */
        private final Class<? extends Extension> extensionType;

        /** A builder for all methods annotated with {@link OnHook} on the extension. */
        private final OnHookContainerModelBuilder hooks;

        private final List<Class<? extends Extension>> dependencies;

        /**
         * Creates a new builder.
         * 
         * @param extensionType
         *            the type of extension we are building a model for
         */
        private Builder(Class<? extends Extension> extensionType) {
            this.extensionType = extensionType;
            this.dependencies = ExtensionDependencyValidator.dependenciesOf(extensionType);
            this.constructor = ConstructorFinder.find(extensionType);
            this.hooks = new OnHookContainerModelBuilder(new ClassProcessor(MethodHandles.lookup(), extensionType, true), ContainerConfiguration.class);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        @SuppressWarnings("unchecked")
        private ExtensionModel<?> build() {
            Class<? extends ExtensionComposer<?>> composerType = null;
            for (Class<?> c : extensionType.getDeclaredClasses()) {
                if (c.getSimpleName().equals("Composer")) {
                    if (!ExtensionComposer.class.isAssignableFrom(c)) {
                        throw new InternalExtensionException(c.getCanonicalName() + " must extend " + StringFormatter.format(ExtensionComposer.class));
                    }
                    composerType = (Class<? extends ExtensionComposer<?>>) c;
                }
            }

            if (composerType != null) {
                ExtensionComposer<?> composer = ConstructorFinder.invoke(composerType);
                ModuleAccess.extension().configureComposer(composer, this);
            }
            // Find all methods annotated with @OnHook on the extension
            return new ExtensionModel<>(this);
        }
    }
}

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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.container.extension.ComposableExtension;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionDeclarationException;
import app.packed.container.extension.ExtensionNode;
import app.packed.container.extension.ExtensionPipelineContext;
import app.packed.container.extension.ExtensionProps;
import app.packed.container.extension.ExtensionWireletPipeline;
import app.packed.contract.Contract;
import app.packed.util.Nullable;
import packed.internal.access.SharedSecrets;
import packed.internal.hook.HookClassBuilder;
import packed.internal.hook.OnHookGroupModel;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.reflect.MemberFinder;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
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

    /** The method handle used to create a new instances. */
    private final MethodHandle constructor;

    /** The type of the extension this model covers. */
    public final Class<? extends Extension> extensionType;

    final OnHookGroupModel hooks;

    final Function<? extends Extension, ? extends ExtensionNode<?>> nodeFactory;

    /** If the extension has a corresponding extension node */
    @Nullable
    private final ExtensionNodeModel node;

    final Map<Class<? extends ExtensionWireletPipeline<?, ?>>, Function<?, ?>> pipelines;

    public final Map<Class<? extends Contract>, BiFunction<?, ExtensionPipelineContext, ?>> constracts;

    public final BiConsumer<? super Extension, ? super app.packed.container.BundleDescriptor.Builder> bundleBuilder;

    /**
     * Creates a new extension model from the specified builder.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionModel(Builder builder) {
        this.constructor = builder.constructor;
        this.extensionType = builder.extensionType;
        this.node = builder.node == null ? null : builder.node.build(this);
        this.hooks = new OnHookGroupModel(builder.hooks, extensionType);
        this.nodeFactory = builder.epc.nodeFactory;
        this.pipelines = Map.copyOf(builder.epc.pipelines);
        this.bundleBuilder = builder.epc.builder;
        this.constracts = Map.copyOf(builder.epc.contracts);
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

    /** A builder for {@link ExtensionModel}. */
    static final class Builder {

        /** An type extractor to find the extension type the node belongs to. */
        private static final TypeVariableExtractor COMPOSABLE_EXTENSION_TV_EXTRACTOR = TypeVariableExtractor.of(ComposableExtension.class);

        /** The constructor used to create a new extension instance. */
        private final MethodHandle constructor;

        final Class<? extends Extension> extensionType;

        @Nullable
        private ExtensionNodeModel.Builder node;

        final HookClassBuilder hooks;
        ExtensionPropsContext epc = new ExtensionPropsContext();

        @SuppressWarnings("unchecked")
        private Builder(Class<? extends Extension> extensionType) {
            this.extensionType = extensionType;
            if (!Modifier.isFinal(extensionType.getModifiers())) {
                throw new ExtensionDeclarationException("The extension '" + StringFormatter.format(extensionType) + "' must be declared final");
            }
            constructor = ConstructorFinder.find(extensionType);
            this.hooks = new HookClassBuilder(extensionType, false);

            if (ComposableExtension.class.isAssignableFrom(extensionType)) {
                Class<? extends ExtensionProps<?>> propsType = (Class<? extends ExtensionProps<?>>) COMPOSABLE_EXTENSION_TV_EXTRACTOR.extract(extensionType);
                // if (!Modifier.isFinal(nodeType.getModifiers())) {
                // throw new ExtensionDeclarationException(
                // "The extension node returned by onAdded(), must be declareda final, node type = " +
                // StringFormatter.format(nodeType));
                // }
                ExtensionProps<?> ep = null;
                MethodHandle mh = ConstructorFinder.find(propsType);
                try {
                    ep = (ExtensionProps<?>) mh.invoke();
                } catch (Throwable e) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e);
                    throw new UndeclaredThrowableException(e);
                }
                SharedSecrets.extension().configureProps(ep, epc);
                requireNonNull(epc.nodeType);
                node = new ExtensionNodeModel.Builder(this, epc.nodeType);
            }
        }

        private ExtensionModel<?> build() {
            MemberFinder.findMethods(Extension.class, extensionType, method -> hooks.processMethod(method));
            return new ExtensionModel<>(this);
        }
    }
}
// Raekkefoelge af installeret extensions....
// Maaske bliver vi noedt til at have @UsesExtension..
// Saa vi kan sige X extension skal koeres foerend Y extension

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
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.ExtensionSidecar;
import app.packed.container.WireletPipeline;
import app.packed.hook.OnHook;
import packed.internal.hook.BaseHookQualifierList;
import packed.internal.hook.OnHookModel;
import packed.internal.reflect.OpenClass;
import packed.internal.sidecar.SidecarModel;
import packed.internal.sidecar.SidecarTypeMeta;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/** A model of an Extension. */
public final class ExtensionSidecarModel extends SidecarModel {

    /** A cache of models. */
    private static final ClassValue<ExtensionSidecarModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionSidecarModel computeValue(Class<?> type) {
            // First, check that the user has specified an actual sub type of Extension to
            // ContainerConfiguration#use() or Bundle#use()
            if (type == Extension.class) {
                throw new IllegalArgumentException(Extension.class.getSimpleName() + ".class is not a valid argument.");
            } else if (!Extension.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(
                        "The specified type '" + StringFormatter.format(type) + "' does not extend '" + StringFormatter.format(Extension.class) + "'");
            }
            return ExtensionModelLoader.load((Class<? extends Extension>) type);
        }
    };

    public static final int ON_CHILDREN_DONE = 2;

    public static final int ON_INSTANTIATION = 0;

    public static final int ON_PREEMBLE = 1;

    final MethodHandle bundleBuilderMethod;

    final Set<Class<? extends Extension>> dependenciesDirect;

    final List<Class<? extends Extension>> dependenciesTotalOrder;

    final BaseHookQualifierList nonActivatingHooks;

    @Nullable
    private final OnHookModel onHookModel;

    /** An optional containing the extension type. To avoid excessive creation of them for {@link Component#extension()}. */
    public final Optional<Class<? extends Extension>> optional;

    final Map<Class<? extends WireletPipeline<?, ?>>, WireletPipelineModel> pipelines;

    /**
     * Creates a new extension model from the specified builder.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionSidecarModel(Builder builder) {
        super(builder);
        this.pipelines = Map.copyOf(builder.pipelines);
        this.bundleBuilderMethod = builder.builderMethod;
        this.dependenciesDirect = Set.copyOf(builder.dependenciesDirect);
        this.dependenciesTotalOrder = builder.dependenciesTotalOrder;
        this.optional = Optional.of(extensionType()); // No need to create an optional every time we need this

        this.onHookModel = builder.onHookModel;
        this.nonActivatingHooks = onHookModel == null ? null : LazyExtensionActivationMap.findNonExtending(onHookModel);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Extension> extensionType() {
        return (Class<? extends Extension>) sidecarType();
    }

    /**
     * Creates a new instance of the extension.
     * 
     * @param context
     *            the extension context that can be constructor injected into the extension
     * @return a new instance of the extension
     */
    public Extension newExtensionInstance(PackedExtensionContext context) {
        // Time goes from around 1000 ns to 12 ns when we cache the method handle.
        // With LambdaMetafactory wrapped in a supplier we can get down to 6 ns
        try {
            if (constructor.type().parameterCount() > 0) {
                return (Extension) constructor.invoke(context);
            } else {
                return (Extension) constructor.invoke();
            }
        } catch (Throwable e) {
            ThrowableUtil.throwIfUnchecked(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Returns an extension model for the specified extension type.
     * 
     * @param extensionType
     *            the type of extension to return a model for
     * @return an extension model for the specified extension type
     */
    public static ExtensionSidecarModel of(Class<? extends Extension> extensionType) {
        return MODELS.get(extensionType);
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

    /** A builder for {@link ExtensionSidecarModel}. */
    static final class Builder extends SidecarModel.Builder {

        /** Meta data about the extension sidecar. */
        private static final SidecarTypeMeta STM = new SidecarTypeMeta(ExtensionSidecar.class, ExtensionSidecar.ON_INSTANTIATION, ExtensionSidecar.ON_PREEMBLE,
                ExtensionSidecar.ON_CHILDREN_DONE);

        /** A list of dependencies on other extensions. */
        private Set<Class<? extends Extension>> dependenciesDirect = new HashSet<>();

        private List<Class<? extends Extension>> dependenciesTotalOrder;

        /** The loader used to load the extension. */
        private final ExtensionModelLoader loader;

        /** A builder for all methods annotated with {@link OnHook} on the extension. */
        private OnHookModel onHookModel;

        final HashMap<Class<? extends WireletPipeline<?, ?>>, WireletPipelineModel> pipelines = new HashMap<>();

        /**
         * Creates a new builder.
         * 
         * @param extensionType
         *            the type of extension we are building a model for
         */
        Builder(Class<? extends Extension> extensionType, ExtensionModelLoader loader) {
            super(STM, extensionType);
            this.loader = requireNonNull(loader);
        }

        /**
         * Builds and returns an extension model.
         * 
         * @return the extension model
         */
        ExtensionSidecarModel build() {
            ExtensionSidecar em = sidecarType.getAnnotation(ExtensionSidecar.class);
            if (em != null) {
                for (Class<? extends Extension> ccc : em.dependencies()) {
                    ExtensionModelLoader.load(ccc, loader);
                    dependenciesDirect.add(ccc);
                }
                for (Class<? extends WireletPipeline<?, ?>> c : em.pipelines()) {
                    WireletPipelineModel m = new WireletPipelineModel.Builder(c).build();
                    pipelines.put(c, m);
                }
            }
            this.dependenciesTotalOrder = OldExtensionUseModel2.totalOrder(sidecarType);

            OpenClass cp = prep();
            this.onHookModel = OnHookModel.newModel(cp, false, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY, ContainerConfiguration.class);
            return new ExtensionSidecarModel(this);
        }
    }

}

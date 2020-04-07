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
import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.PipelineWirelet;
import app.packed.container.UseExtension;
import app.packed.container.WireletPipeline;
import packed.internal.reflect.OpenClass;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.UncheckedThrowableFactory;

/** A model of a {@link WireletPipeline}. */
public final class WireletPipelineModel {

    /** A cache of models for each pipeline implementation. */
    private static final ClassValue<WireletPipelineModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected WireletPipelineModel computeValue(Class<?> type) {
            return new WireletPipelineModel.Builder((Class<? extends WireletPipeline<?, ?>>) type).build();
        }
    };

    /** A method handle to create new pipeline instances. */
    private final MethodHandle constructor;

    /** Any extension this pipeline is a part of. */
    @Nullable
    private final Class<? extends Extension> extensionType;

    /** The type of pipeline. */
    final Class<? extends WireletPipeline<?, ?>> type;

    /**
     * Create a new model.
     * 
     * @param builder
     *            the builder to use for creating a new model
     */
    private WireletPipelineModel(Builder builder) {
        this.type = builder.actualType;
        this.extensionType = builder.extension == null ? null : builder.extension.extensionType();
        this.constructor = requireNonNull(builder.constructor);
    }

    /** Any extension this pipeline is a part of. */
    @Nullable
    public Class<? extends Extension> extensionType() {
        return extensionType;
    }

    /**
     * Creates a new pipeline instance.
     * 
     * @return a new pipeline instance
     */
    WireletPipeline<?, ?> newPipeline(Extension extension) {
        try {
            if (constructor.type().parameterCount() == 0) {
                return (WireletPipeline<?, ?>) constructor.invoke();
            } else {
                return (WireletPipeline<?, ?>) constructor.invoke(extension);
            }
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Returns a model for the specified pipeline type.
     * 
     * @param type
     *            the pipeline type to return a model for.
     * @return the model
     */
    public static WireletPipelineModel of(Class<? extends WireletPipeline<?, ?>> type) {
        return MODELS.get(type);
    }

    /**
     * Returns a model for the specified extension wirelet type.
     * 
     * @param wireletType
     *            the extension wirelet type to return a model for.
     * @return the model
     */
    static WireletPipelineModel ofWirelet(Class<? extends PipelineWirelet<?>> wireletType) {
        return PipelineWireletModel.MODELS.get(wireletType).pipeline;
    }

    /** A builder of {@link WireletPipelineModel}. */
    private static class Builder {

        private final Class<? extends WireletPipeline<?, ?>> actualType;

        private MethodHandle constructor;

        @Nullable
        private ExtensionSidecarModel extension;

        /**
         * @param type
         */
        private Builder(Class<? extends WireletPipeline<?, ?>> type) {
            this.actualType = requireNonNull(type);
        }

        private WireletPipelineModel build() {
            OpenClass cp = new OpenClass(MethodHandles.lookup(), actualType, true);
            Constructor<?> c = actualType.getDeclaredConstructors()[0];

            UseExtension ue = actualType.getAnnotation(UseExtension.class);
            if (ue != null) {
                // TODO validate same module... Maybe put this in a UseExtensionHelper class
                extension = ExtensionSidecarModel.of(ue.value());
            }

            this.constructor = cp.unreflectConstructor(c, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);

            // I think we need to validate that the pipeline is specified in
            return new WireletPipelineModel(this);
        }
    }

    /** A model for {@link PipelineWirelet} types. */
    private static final class PipelineWireletModel {

        /** A cache of models. */
        private static final ClassValue<PipelineWireletModel> MODELS = new ClassValue<>() {

            /** {@inheritDoc} */
            @SuppressWarnings("unchecked")
            @Override
            protected PipelineWireletModel computeValue(Class<?> type) {
                return new PipelineWireletModel((Class<? extends PipelineWirelet<?>>) type);
            }
        };

        /** A type variable extractor to extract what kind of pipeline an extension wirelet belongs to. */
        private static final TypeVariableExtractor PIPELINE_TYPE_EXTRACTOR = TypeVariableExtractor.of(PipelineWirelet.class);

        /** The extension pipeline the wirelet belongs to. */
        private final WireletPipelineModel pipeline;

        /**
         * @param type
         *            the type of extension wirelet
         */
        @SuppressWarnings("unchecked")
        private PipelineWireletModel(Class<? extends PipelineWirelet<?>> type) {
            Class<? extends WireletPipeline<?, ?>> p = (Class<? extends WireletPipeline<?, ?>>) PIPELINE_TYPE_EXTRACTOR.extract(type);
            this.pipeline = WireletPipelineModel.of(p);
            // Should check that UseExtension is not used on the PipelineWirelet...
        }
    }
}

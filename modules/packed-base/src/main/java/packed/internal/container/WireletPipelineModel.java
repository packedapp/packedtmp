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
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.container.PipelineWirelet;
import app.packed.container.UseExtension;
import app.packed.container.WireletPipeline;
import packed.internal.reflect.OpenClass;
import packed.internal.reflect.t2.FindConstructor;
import packed.internal.reflect.t2.InjectionSpec;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

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

    /** A cache of wirelet types to pipeline models. */
    private static final ClassValue<WireletPipelineModel> WIRELET_MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected WireletPipelineModel computeValue(Class<?> type) {
            Class<? extends WireletPipeline<?, ?>> p = (Class<? extends WireletPipeline<?, ?>>) WIRELET_TO_PIPELINE_TYPE_EXTRACTOR.extract(type);
            return WireletPipelineModel.of(p);
        }
    };

    /** A type variable extractor to extract what kind of pipeline a pipeline wirelet belongs to. */
    private static final TypeVariableExtractor WIRELET_TO_PIPELINE_TYPE_EXTRACTOR = TypeVariableExtractor.of(PipelineWirelet.class);

    /** A method handle to create new pipeline instances. */
    private final MethodHandle constructor;

    /** Any extension this pipeline is a part of. */
    @Nullable
    private final Class<? extends Extension> extensionType;

    /** The type of pipeline this model represents. */
    private final Class<? extends WireletPipeline<?, ?>> type;

    /**
     * Create a new model.
     * 
     * @param builder
     *            the builder to use for creating a new model
     */
    private WireletPipelineModel(Builder builder) {
        this.type = builder.type;
        this.extensionType = builder.extension == null ? null : builder.extension.extensionType();
        this.constructor = requireNonNull(builder.constructor);
    }

    /** Any extension this pipeline is a part of (may be null). */
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
     * Returns the type of pipeline this model represents.
     * 
     * @return the type of pipeline this model represents
     */
    public Class<? extends WireletPipeline<?, ?>> type() {
        return type;
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
        return WIRELET_MODELS.get(wireletType);
    }

    /** A builder of {@link WireletPipelineModel}. */
    private static class Builder {

        private MethodHandle constructor;

        /** Any extension the pipeline is a part of. */
        @Nullable
        private ExtensionSidecarModel extension;

        /** The type of pipeline. */
        private final Class<? extends WireletPipeline<?, ?>> type;

        /**
         * Creates a new builder
         * 
         * @param type
         *            the pipeline type
         */
        private Builder(Class<? extends WireletPipeline<?, ?>> type) {
            this.type = requireNonNull(type);
        }

        private WireletPipelineModel build() {
            UseExtension ue = type.getAnnotation(UseExtension.class);
            if (ue != null) {
                Class<? extends Extension> eType = ue.value();
                if (type.getModule() != eType.getModule()) {
                    throw new InternalExtensionException("The extension " + eType + " and pipeline " + type + " must be defined in the same module, was "
                            + eType.getModule() + " and " + type.getModule());
                }
                extension = ExtensionSidecarModel.of(eType);
            }

            // FindConstructor...
            OpenClass cp = new OpenClass(MethodHandles.lookup(), type, true);

            InjectionSpec is = new InjectionSpec(type, extension == null ? Extension.class : extension.extensionType());

            // Constructor<?> c = type.getDeclaredConstructors()[0];
            // this.constructor = cp.unreflectConstructor(c, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            this.constructor = new FindConstructor().doIt(cp, is);
            return new WireletPipelineModel(this);
        }
    }
}

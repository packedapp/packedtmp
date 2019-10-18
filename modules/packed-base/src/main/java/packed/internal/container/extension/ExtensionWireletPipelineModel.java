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

import java.util.function.BiFunction;

import app.packed.container.Extension;
import app.packed.container.ExtensionWirelet;
import app.packed.container.ExtensionWirelet.Pipeline;
import packed.internal.container.MutableWireletList;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/** A descriptor for an {@link Pipeline}. */
public final class ExtensionWireletPipelineModel {

    /** A cache of values. */
    private static final ClassValue<ExtensionWireletPipelineModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected ExtensionWireletPipelineModel computeValue(Class<?> type) {
            return new ExtensionWireletPipelineModel.Builder((Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>>) type).build();
        }
    };

    /** An extractor to find the extension the node is build upon. */
    private static final TypeVariableExtractor EXTENSION_NODE_TV_EXTRACTOR = TypeVariableExtractor.of(ExtensionWirelet.Pipeline.class, 0);

    /** The extension model for this pipeline. */
    private final ExtensionModel<?> extension;

    /** The factory used for creating new pipeline instances. */
    private final BiFunction<?, ?, ?> factory;

    /** The type of pipeline. */
    private final Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> type;

    /**
     * @param builder
     */
    @SuppressWarnings("unchecked")
    private ExtensionWireletPipelineModel(Builder builder) {
        this.type = builder.actualType;
        Class<? extends Extension> extensionType = (Class<? extends Extension>) EXTENSION_NODE_TV_EXTRACTOR.extract(builder.actualType);
        this.extension = ExtensionModel.of(extensionType);
        this.factory = requireNonNull(extension.pipelines.get(type));
    }

    /**
     * Returns the extension model for this pipeline.
     * 
     * @return the extension model for this pipeline
     */
    public ExtensionModel<?> extension() {
        return extension;
    }

    /**
     * Creates a new pipeline instance.
     * 
     * @return a new pipeline instance
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ExtensionWirelet.Pipeline<?, ?, ?> newPipeline(Extension extension, MutableWireletList<?> wirelets) {
        return (ExtensionWirelet.Pipeline<?, ?, ?>) ((BiFunction) factory).apply(extension, wirelets);
    }

    /**
     * Returns the type of pipeline.
     * 
     * @return the type of pipeline
     */
    public Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> type() {
        return type;
    }

    /**
     * Returns a model for the specified pipeline type.
     * 
     * @param type
     *            the pipeline type to return a model for.
     * @return the model
     */
    public static ExtensionWireletPipelineModel of(Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> type) {
        return CACHE.get(type);
    }

    /**
     * Returns a model for the specified extension wirelet type.
     * 
     * @param wireletType
     *            the extension wirelet type to return a model for.
     * @return the model
     */
    public static ExtensionWireletPipelineModel ofWireletType(Class<? extends ExtensionWirelet<?>> wireletType) {
        return ExtensionWireletModel.CACHE.get(wireletType).pipeline;
    }

    private static class Builder {

        private final Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> actualType;

        /**
         * @param type
         */
        private Builder(Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> type) {
            actualType = requireNonNull(type);
        }

        ExtensionWireletPipelineModel build() {
            return new ExtensionWireletPipelineModel(this);
        }
    }

    /**
     * A model for a class extending {@link ExtensionWirelet}. Is currently only used to extract the type variable from the
     * wirelet. Which points to the pipeline it is a part of.
     */
    // Right now this is just a static class, because I'm unsure whether or not we will cache other information than the
    // type parameter
    // to ExtensionWirelet
    private static class ExtensionWireletModel {

        /** A cache of values. */
        private static final ClassValue<ExtensionWireletModel> CACHE = new ClassValue<>() {

            /** {@inheritDoc} */
            @SuppressWarnings("unchecked")
            @Override
            protected ExtensionWireletModel computeValue(Class<?> type) {
                return new ExtensionWireletModel((Class<? extends ExtensionWirelet<?>>) type);
            }
        };

        /** A type variable extractor to extract what kind of pipeline an extension wirelet belongs to. */
        private static final TypeVariableExtractor EXTENSION_TYPE_EXTRACTOR = TypeVariableExtractor.of(ExtensionWirelet.class);

        private final ExtensionWireletPipelineModel pipeline;

        /**
         * @param type
         */
        @SuppressWarnings("unchecked")
        private ExtensionWireletModel(Class<? extends ExtensionWirelet<?>> type) {
            Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> extensionType = (Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>>) EXTENSION_TYPE_EXTRACTOR
                    .extract(type);
            this.pipeline = ExtensionWireletPipelineModel.of(extensionType);
        }
    }
}

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

import app.packed.container.ExtensionWirelet;
import app.packed.container.ExtensionWireletPipeline;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/** A model for {@link ExtensionWirelet} types. */
final class ExtensionWireletModel {

    /** A cache of models. */
    private static final ClassValue<ExtensionWireletModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionWireletModel computeValue(Class<?> type) {
            return new ExtensionWireletModel((Class<? extends ExtensionWirelet<?>>) type);
        }
    };

    /** A type variable extractor to extract what kind of pipeline an extension wirelet belongs to. */
    private static final TypeVariableExtractor PIPELINE_TYPE_EXTRACTOR = TypeVariableExtractor.of(ExtensionWirelet.class);

    /** The extension the wirelet belongs to. */
    final ExtensionModel<?> extension;

    /** The extension pipeline the wirelet belongs to. */
    final ExtensionWireletPipelineModel pipeline;

    /**
     * @param type
     *            the type of extension wirelet
     */
    @SuppressWarnings("unchecked")
    private ExtensionWireletModel(Class<? extends ExtensionWirelet<?>> type) {
        Class<? extends ExtensionWireletPipeline<?, ?, ?>> pipelineType = (Class<? extends ExtensionWireletPipeline<?, ?, ?>>) PIPELINE_TYPE_EXTRACTOR
                .extract(type);
        this.pipeline = ExtensionWireletPipelineModel.of(pipelineType);
        this.extension = ExtensionModel.of(pipeline.extensionType);
    }

    /**
     * Returns a model for the specified extension wirelet type.
     * 
     * @param wireletType
     *            the extension wirelet type to return a model for.
     * @return the model
     */
    static ExtensionWireletPipelineModel of(Class<? extends ExtensionWirelet<?>> wireletType) {
        return ExtensionWireletModel.CACHE.get(wireletType).pipeline;
    }
}
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

import app.packed.container.PipelineWirelet;
import app.packed.container.WireletPipeline;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/** A model for {@link PipelineWirelet} types. */
final class PipelineWireletModel {

    /** A cache of models. */
    private static final ClassValue<PipelineWireletModel> CACHE = new ClassValue<>() {

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
        Class<? extends WireletPipeline<?, ?, ?>> p = (Class<? extends WireletPipeline<?, ?, ?>>) PIPELINE_TYPE_EXTRACTOR.extract(type);
        this.pipeline = WireletPipelineModel.of(p);
        // Should check that UseExtension is not used on the PipelineWirelet...
    }

    /**
     * Returns a model for the specified extension wirelet type.
     * 
     * @param wireletType
     *            the extension wirelet type to return a model for.
     * @return the model
     */
    static WireletPipelineModel of(Class<? extends PipelineWirelet<?>> wireletType) {
        return PipelineWireletModel.CACHE.get(wireletType).pipeline;
    }
}
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

import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.ExtensionWireletPipeline;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/**
 *
 */
public class ExtensionWireletModel {

    /** A cache of values. */
    private static final ClassValue<ExtensionWireletModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionWireletModel computeValue(Class<?> type) {
            return new ExtensionWireletModel((Class<? extends ExtensionWirelet<?>>) type);
        }
    };

    static final TypeVariableExtractor EXTENSION_TYPE_EXTRACTOR = TypeVariableExtractor.of(ExtensionWirelet.class);

    public final Class<? extends ExtensionWireletPipeline<?>> extensionType;

    public final ExtensionWireletPipelineModel pipeline;

    /**
     * @param type
     */
    @SuppressWarnings("unchecked")
    private ExtensionWireletModel(Class<? extends ExtensionWirelet<?>> type) {
        extensionType = (Class<? extends ExtensionWireletPipeline<?>>) EXTENSION_TYPE_EXTRACTOR.extract(type);
        this.pipeline = ExtensionWireletPipelineModel.of(extensionType);
    }

    public static ExtensionWireletModel of(Class<? extends ExtensionWirelet<?>> wireletType) {
        return CACHE.get(wireletType);
    }
}

/// ** An type variable extractor to extract the type of pipeline the extension wirelet needs. */
// private static final TypeVariableExtractor ARTIFACT_DRIVER_TV_EXTRACTOR =
/// TypeVariableExtractor.of(ExtensionWirelet.class);
//
// private static final ClassValue<Class<? extends Extension>> WIRELET_TO_EXTENSION = new ClassValue<>() {
//
// @Override
// protected Class<? extends Extension> computeValue(Class<?> type) {
// Type t = ARTIFACT_DRIVER_TV_EXTRACTOR.extract(type);
// if (!(t instanceof Class)) {
// throw new IllegalStateException();
// }
// @SuppressWarnings("unchecked")
// Class<? extends Extension> extensionType = (Class<? extends Extension>) t;
// if (extensionType.getModule() != type.getModule()) {
// throw new IllegalArgumentException("The wirelet and the extension must be defined in the same module, however
/// extension "
// + StringFormatter.format(extensionType) + " was defined in " + extensionType.getModule() + ", and this wirelet type "
// + StringFormatter.format(getClass()) + " was defined in module " + getClass().getModule());
// }
// return extensionType;
// }
// };
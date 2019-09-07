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

import java.lang.reflect.Method;

import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionPipeline;
import app.packed.container.extension.ExtensionWirelet;
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

    public final Class<? extends Extension> extensionType;

    /**
     * @param type
     */
    @SuppressWarnings("unchecked")
    private ExtensionWireletModel(Class<? extends ExtensionWirelet<?>> type) {
        extensionType = (Class<? extends Extension>) EXTENSION_TYPE_EXTRACTOR.extract(type);

        // Check that the method is overrriden....
        Method newPipeline = null;
        try {
            newPipeline = extensionType.getDeclaredMethod("newPipeline");
        } catch (NoSuchMethodException ignore) {
            throw new Error();
        }
        System.out.println(newPipeline);
    }

    public static ExtensionWireletModel of(Class<? extends Wirelet> wireletType) {
        return CACHE.get(wireletType);
    }

    public static ExtensionPipeline<?> newPipeline(Extension extension, Class<? extends Wirelet> wireletType) {
        ExtensionPipelineModel epm = ExtensionPipelineModel.CACHE.get(wireletType);
        try {
            return (ExtensionPipeline<?>) epm.constructor.invoke(extension);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

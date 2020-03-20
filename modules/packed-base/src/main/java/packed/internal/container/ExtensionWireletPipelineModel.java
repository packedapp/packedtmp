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

import app.packed.container.Extension;
import app.packed.container.ExtensionWirelet;
import app.packed.container.ExtensionWirelet.Pipeline;
import packed.internal.reflect.OpenClass;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.UncheckedThrowableFactory;

/** A descriptor for an {@link Pipeline}. */
final class ExtensionWireletPipelineModel {

    /** A cache of values. */
    private static final ClassValue<ExtensionWireletPipelineModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected ExtensionWireletPipelineModel computeValue(Class<?> type) {
            return ExtensionModelLoader.pipeline((Class<? extends Pipeline<?, ?, ?>>) type);
        }
    };

    /** An extractor to find the extension the node is build upon. */
    private static final TypeVariableExtractor EXTENSION_NODE_TV_EXTRACTOR = TypeVariableExtractor.of(ExtensionWirelet.Pipeline.class, 0);

    /** A method handle to create new pipeline instances. */
    private final MethodHandle constructor;

    /** The type of extension this pipeline is a part of. */
    final Class<? extends Extension> extensionType;

    /** The type of pipeline. */
    final Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> type;

    /**
     * Create a new model.
     * 
     * @param builder
     *            the builder to use for creating a new model
     */
    private ExtensionWireletPipelineModel(Builder builder) {
        this.type = builder.actualType;
        this.extensionType = extensionTypeOf(builder.actualType);
        this.constructor = requireNonNull(builder.constructor);
    }

    /**
     * Creates a new pipeline instance.
     * 
     * @return a new pipeline instance
     */
    ExtensionWirelet.Pipeline<?, ?, ?> newPipeline(Extension extension) {
        try {
            if (constructor.type().parameterCount() == 0) {
                return (Pipeline<?, ?, ?>) constructor.invoke();
            } else {
                return (Pipeline<?, ?, ?>) constructor.invoke(extension);
            }
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static Class<? extends Extension> extensionTypeOf(Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> pipelineType) {
        return (Class<? extends Extension>) EXTENSION_NODE_TV_EXTRACTOR.extract(pipelineType);
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

    /** A builder of {@link ExtensionWireletPipelineModel}. */
    static class Builder {

        private final Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> actualType;

        private MethodHandle constructor;

        /**
         * @param type
         */
        Builder(Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> type) {
            actualType = requireNonNull(type);
        }

        ExtensionWireletPipelineModel build() {
            OpenClass cp = new OpenClass(MethodHandles.lookup(), actualType, true);
            Constructor<?> c = actualType.getDeclaredConstructors()[0];

            this.constructor = cp.unreflectConstructor(c, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);

            // this.constructor = ConstructorFinder.find(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);

            // I think we need to validate that the pipeline is specified in

            return new ExtensionWireletPipelineModel(this);
        }
    }
}

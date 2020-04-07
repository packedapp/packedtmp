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
import app.packed.container.UseExtension;
import app.packed.container.WireletPipeline;
import packed.internal.reflect.OpenClass;
import packed.internal.util.UncheckedThrowableFactory;

/** A model of a {@link WireletPipeline}. */
public final class WireletPipelineModel {

    /** A cache of models for each pipeline implementation. */
    private static final ClassValue<WireletPipelineModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected WireletPipelineModel computeValue(Class<?> type) {
            return ExtensionModelLoader.pipeline((Class<? extends WireletPipeline<?, ?>>) type);
        }
    };

    /** A method handle to create new pipeline instances. */
    private final MethodHandle constructor;

    /** Any extension this pipeline is a part of. */
    @Nullable
    public final Class<? extends Extension> extensionType;

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
        this.extensionType = extensionTypeOf(builder.actualType);
        this.constructor = requireNonNull(builder.constructor);
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

    @Nullable
    static Class<? extends Extension> extensionTypeOf(Class<? extends WireletPipeline<?, ?>> pipelineType) {
        UseExtension ue = pipelineType.getAnnotation(UseExtension.class);
        return ue == null ? null : ue.value();
    }

    /**
     * Returns a model for the specified pipeline type.
     * 
     * @param type
     *            the pipeline type to return a model for.
     * @return the model
     */
    public static WireletPipelineModel of(Class<? extends WireletPipeline<?, ?>> type) {
        return CACHE.get(type);
    }

    /** A builder of {@link WireletPipelineModel}. */
    public static class Builder {

        private final Class<? extends WireletPipeline<?, ?>> actualType;

        private MethodHandle constructor;

        /**
         * @param type
         */
        public Builder(Class<? extends WireletPipeline<?, ?>> type) {
            actualType = requireNonNull(type);
        }

        WireletPipelineModel build() {
            OpenClass cp = new OpenClass(MethodHandles.lookup(), actualType, true);
            Constructor<?> c = actualType.getDeclaredConstructors()[0];

            this.constructor = cp.unreflectConstructor(c, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);

            // I think we need to validate that the pipeline is specified in
            return new WireletPipelineModel(this);
        }
    }
}

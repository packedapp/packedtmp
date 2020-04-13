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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.PipelineWirelet;
import app.packed.container.WireletPipeline;
import packed.internal.reflect.InjectionSpec;
import packed.internal.reflect.OpenClass;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/** A model of a {@link WireletPipeline}. */
public final class WireletPipelineModel {

    /** A cache of models for each pipeline implementation. */
    private static final ClassValue<WireletPipelineModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected WireletPipelineModel computeValue(Class<?> type) {
            return new WireletPipelineModel((Class<? extends WireletPipeline<?, ?>>) type);
        }
    };

    /** A cache of pipeline wirelet types to pipeline models. */
    private static final ClassValue<WireletPipelineModel> WIRELET_TYPE_TO_MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected WireletPipelineModel computeValue(Class<?> type) {
            Class<? extends WireletPipeline<?, ?>> p = (Class<? extends WireletPipeline<?, ?>>) WIRELET_TYPE_TO_PIPELINE_TYPE_EXTRACTOR.extract(type);
            return WireletPipelineModel.of(p);
        }
    };

    /** A type variable extractor to extract what kind of pipeline a pipeline wirelet belongs to. */
    private static final TypeVariableExtractor WIRELET_TYPE_TO_PIPELINE_TYPE_EXTRACTOR = TypeVariableExtractor.of(PipelineWirelet.class);

    /** A method handle to create new pipeline instances. */
    private final MethodHandle constructor;

    /** Any extension this pipeline is a member of. */
    @Nullable
    private final Class<? extends Extension> memberOfExtension;

    /** The type of pipeline this model represents. */
    private final Class<? extends WireletPipeline<?, ?>> type;

    /**
     * Create a new model.
     * 
     * @param type
     *            the type of pipeline
     */
    private WireletPipelineModel(Class<? extends WireletPipeline<?, ?>> type) {
        this.type = type; // should we check type assignable???
        this.memberOfExtension = ExtensionSidecarModel.findIfMember(type);

        OpenClass cp = new OpenClass(MethodHandles.lookup(), type, true);
        InjectionSpec is = new InjectionSpec(type, memberOfExtension == null ? Extension.class : memberOfExtension);
        if (memberOfExtension != null) {
            is.add(memberOfExtension, 0);
        }
        this.constructor = cp.findConstructor(is);
    }

    /** Any extension this pipeline is a member of (may be null). */
    @Nullable
    public Class<? extends Extension> memberOfExtension() {
        return memberOfExtension;
    }

    /**
     * Creates a new pipeline instance.
     * 
     * @param extension
     *            An extension instance if the pipeline is a member of an extension. Otherwise null must be provided.
     * @return a new pipeline instance
     */
    WireletPipeline<?, ?> newPipeline(@Nullable Extension extension) {
        try {
            return (WireletPipeline<?, ?>) constructor.invoke(extension);
        } catch (Throwable e) {
            // Yes how do we handle this...
            // Probably different whether or not we are member of an extension...
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
     * Returns a model for the specified wirelet type.
     * 
     * @param wireletType
     *            the wirelet type to return a model for.
     * @return the model
     */
    static WireletPipelineModel ofWirelet(Class<? extends PipelineWirelet<?>> wireletType) {
        return WIRELET_TYPE_TO_MODELS.get(wireletType);
    }
}

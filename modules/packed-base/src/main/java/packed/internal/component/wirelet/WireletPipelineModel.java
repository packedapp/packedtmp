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
package packed.internal.component.wirelet;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import app.packed.component.WireletPipeline;
import app.packed.container.Extension;
import packed.internal.container.ExtensionModel;
import packed.internal.reflect.MethodHandleBuilder;
import packed.internal.reflect.OpenClass;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.sidecar.Model;
import packed.internal.util.ThrowableUtil;

/** A model of a {@link WireletPipeline}. */
// Extends WireletModel if pipeline-> wirelet
public final class WireletPipelineModel extends Model {

    /** A cache of models for each pipeline implementation. */
    private static final ClassValue<WireletPipelineModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected WireletPipelineModel computeValue(Class<?> type) {
            return new WireletPipelineModel((Class<? extends WireletPipeline<?, ?>>) type);
        }
    };

    /** A type variable extractor to extract what kind of pipeline a pipeline wirelet belongs to. */
    // TODO replace with check that wirelets match pipeline type..
    static final TypeVariableExtractor WIRELET_TYPE_TO_PIPELINE_TYPE_EXTRACTOR = TypeVariableExtractor.of(WireletPipeline.class);

    /** A method handle to create new pipeline instances. */
    private final MethodHandle constructor;

    /** Any extension this pipeline is a member of. */
    @Nullable
    private final Class<? extends Extension> extension;

    /**
     * Create a new model.
     * 
     * @param type
     *            the type of pipeline
     */
    private WireletPipelineModel(Class<? extends WireletPipeline<?, ?>> type) {
        super(type);
        this.extension = ExtensionModel.findAnyExtensionMember(type);

        OpenClass cp = new OpenClass(MethodHandles.lookup(), type, true);
        MethodHandleBuilder dim = MethodHandleBuilder.of(WireletPipeline.class, Extension.class);
        if (extension != null) {
            dim.addKey(extension, 0);
        }
        this.constructor = cp.findConstructor(dim);
    }

    /** Any extension this pipeline is a member of (may be null). */
    @Nullable
    public Class<? extends Extension> extension() {
        return extension;
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
            return (WireletPipeline<?, ?>) constructor.invokeExact(extension);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);// Probably different whether or not we are member of an extension...
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
}
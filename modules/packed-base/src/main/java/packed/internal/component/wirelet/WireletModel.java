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

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.component.WireletPipeline;
import app.packed.component.WireletSidecar;
import app.packed.container.Extension;
import app.packed.container.ExtensionMember;
import app.packed.container.InternalExtensionException;
import packed.internal.container.ExtensionModel;
import packed.internal.sidecar.Model;

/** A model of a {@link Wirelet}. This class is public because of {@link NoWireletPipeline}. */
public final class WireletModel extends Model {

    /** A cache of models for {@link Wirelet} subclasses. */
    private static final ClassValue<WireletModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected WireletModel computeValue(Class<?> type) {
            return new WireletModel((Class<? extends Wirelet>) type);
        }
    };

    private final boolean inherited;

    /** Any extension this wirelet is a member of. */
    @Nullable
    private final Class<? extends Extension> extension;

    /** Any pipeline the wirelet might belong to. */
    @Nullable
    private final WireletPipelineModel pipeline;

    public final boolean requireAssemblyTime;

    /**
     * Create a new wirelet model.
     * 
     * @param type
     *            the wirelet type
     */
    private WireletModel(Class<? extends Wirelet> type) {
        super(type);
        this.extension = ExtensionModel.findAnyExtensionMember(type);

        // Let's see if the wirelet has an annotation
        WireletSidecar ws = type.getAnnotation(WireletSidecar.class);
        if (ws != null) {
            this.inherited = false;// ws.inherited();
            this.requireAssemblyTime = ws.failOnImage();

            // Find any pipeline this wirelet is part of
            Class<? extends WireletPipeline<?, ?>> p = ws.pipeline();
            if (p != NoWireletPipeline.class) {
                this.pipeline = WireletPipelineModel.of(p);
                // XXX must be assignable to YY to be a part of the pipeline
                if (pipeline.extension() != extension) {
                    throw new InternalExtensionException("The wirelet " + type + " and the pipeline " + p + " must both be annotated with @"
                            + ExtensionMember.class.getSimpleName() + " and the same extension was" + extension + " and " + pipeline.extension());
                }
            } else {
                this.pipeline = null;
            }
        } else { // No annotation, use default values.
            this.pipeline = null;
            this.inherited = false;
            this.requireAssemblyTime = false;
        }
    }

    /**
     * Returns whether or not the wirelet is inherited. This is currently always false. See OneNote Wont Fix - Inherited
     * Wirelets for details.
     * 
     * @return whether or not the wirelet is inherited
     */
    public boolean inherited() {
        return inherited;
    }

    /**
     * Returns any pipeline this wirelet is a part of.
     * 
     * @return any pipeline this wirelet is a part of
     */
    @Nullable
    WireletPipelineModel pipeline() {
        return pipeline;
    }

    /**
     * Returns a model for the specified wirelet type.
     * 
     * @param wireletType
     *            the wirelet type to return a model for.
     * @return the model
     */
    public static WireletModel of(Class<? extends Wirelet> wireletType) {
        return MODELS.get(wireletType);
    }

    /** A dummy class indicating no pipeline for {@link WireletSidecar#pipeline()}. */
    public static final class NoWireletPipeline extends WireletPipeline<NoWireletPipeline, Wirelet> {
        /** No instantiation. */
        private NoWireletPipeline() {}
    }
}
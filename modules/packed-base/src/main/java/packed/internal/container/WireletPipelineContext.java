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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.component.WireletPipeline;
import app.packed.container.Extension;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** A context for each {@link WireletPipeline} instance. */
public final class WireletPipelineContext {

    /** A MethodHandle that can invoke ContainerBundle#configure. */
    private static final MethodHandle MH_WIRELET_PIPELINE_INITIALIZE = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), WireletPipeline.class, "initialize",
            void.class, WireletPipelineContext.class);

    /** The pipeline instance that can be injected. */
    public WireletPipeline<?, ?> instance;

    /** A model of the pipeline */
    private final WireletPipelineModel model;

    /** Any previous pipeline. */
    @Nullable
    private final WireletPipelineContext previous;

    /** The wirelets making up the pipeline */
    final ArrayList<Wirelet> wirelets = new ArrayList<>();

    WireletPipelineContext(WireletPipelineModel model, @Nullable WireletPipelineContext previous) {
        this.model = requireNonNull(model);
        this.previous = previous;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void forEach(Consumer<?> action) {
        requireNonNull("action is null");
        if (previous != null) {
            previous.forEach(action);
        }
        wirelets.forEach((Consumer) action);
    }

    @SuppressWarnings("unchecked")
    public void forEachInPipeline(@SuppressWarnings("rawtypes") Consumer action) {
        requireNonNull("action is null");
        wirelets.forEach(action);
    }

    void instantiate(@Nullable Extension extension) {
        instance = model.newPipeline(extension);
        try {
            MH_WIRELET_PIPELINE_INITIALIZE.invoke(instance, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * Returns any extension this pipeline is a member of. Or null if it does not belong to an extension.
     * 
     * @return any extension this pipeline is a member of
     */
    @Nullable
    Class<? extends Extension> extension() {
        return model.extension();
    }

    /**
     * Returns any previous pipeline. Or null if there is no previous pipeline.
     * 
     * @return any previous pipeline.
     */
    public WireletPipeline<?, ?> previous() {
        WireletPipelineContext p = previous;
        return p == null ? null : p.instance;
    }

    @SuppressWarnings("unchecked")
    public List<?> toList() {
        @SuppressWarnings("rawtypes")
        ArrayList l = new ArrayList<>();
        forEach(w -> l.add(w));
        return l;
    }
}

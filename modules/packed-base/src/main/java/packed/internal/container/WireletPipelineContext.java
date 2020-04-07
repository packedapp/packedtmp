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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.PipelineWirelet;
import app.packed.container.WireletPipeline;
import packed.internal.moduleaccess.ModuleAccess;

/**
 *
 */
public final class WireletPipelineContext {

    // Vi gennem den saa folk kan faa den injected..
    public WireletPipeline<?, ?> instance;

    final WireletPipelineModel model;

    @Nullable
    public WireletPipelineContext previous;

    final ArrayList<PipelineWirelet<?>> wirelets = new ArrayList<>();

    WireletPipelineContext(WireletPipelineModel model, @Nullable WireletPipelineContext previous) {
        this.model = requireNonNull(model);
        this.previous = previous;
    }

    Class<? extends Extension> extension() {
        return model.extensionType();
    }

    @SuppressWarnings("unchecked")
    public void forEach(@SuppressWarnings("rawtypes") Consumer action) {
        requireNonNull("action is null");
        if (previous != null) {
            previous.forEach(action);
        }
        wirelets.forEach(action);
    }

    @SuppressWarnings("unchecked")
    public void forEachInPipeline(@SuppressWarnings("rawtypes") Consumer action) {
        requireNonNull("action is null");
        wirelets.forEach(action);
    }

    @SuppressWarnings("unchecked")
    public List<?> toList() {
        @SuppressWarnings("rawtypes")
        ArrayList l = new ArrayList<>();
        forEach(w -> l.add(w));
        return l;
    }

    void instantiate(@Nullable Extension extension) {
        instance = model.newPipeline(extension);
        ModuleAccess.extension().pipelineInitialize(this);
    }
}

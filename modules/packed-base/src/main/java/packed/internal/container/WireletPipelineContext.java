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
import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.container.PipelineWirelet;
import app.packed.container.WireletPipeline;

/**
 *
 */
public final class WireletPipelineContext {

    // Vi gennem den saa folk kan faa den injected..
    WireletPipeline<?, ?, ?> instance;

    final WireletPipelineModel model;

    @Nullable
    WireletPipelineContext previous;

    final ArrayList<PipelineWirelet<?>> wirelets = new ArrayList<>();

    WireletPipelineContext(WireletPipelineModel model, @Nullable WireletPipelineContext previous) {
        this.model = requireNonNull(model);
        this.previous = previous;
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
}

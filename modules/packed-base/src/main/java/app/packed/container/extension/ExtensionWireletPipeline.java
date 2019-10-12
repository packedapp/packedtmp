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
package app.packed.container.extension;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

/**
 * Extension wirelet pipelines
 * 
 * If a given extension allows specific wirelets to late configure it after. You need to use a pipeline.
 */
// Kan only define one pipeline per extension... Maybe...
// Kunne jo godt have @Nullable GreenPipeline, @Nullable BlackPipeline
public abstract class ExtensionWireletPipeline<E extends Extension, P extends ExtensionWireletPipeline<E, P, W>, W extends ExtensionWirelet<P>> {

    private final E extension;

    /** Any previous pipeline. */
    private final Optional<P> previous;

    /** A list initially containing the wirelets that was used to create this pipeline. */
    private final ExtensionWireletList<W> wirelets;

    protected ExtensionWireletPipeline(E extension, ExtensionWireletList<W> wirelets) {
        this.extension = requireNonNull(extension, "extension is null");
        this.wirelets = requireNonNull(wirelets, "wirelets is null");
        this.previous = Optional.empty();
    }

    protected ExtensionWireletPipeline(P from, ExtensionWireletList<W> wirelets) {
        this.extension = from.extension();
        this.wirelets = requireNonNull(wirelets, "wirelets is null");
        this.previous = Optional.of(from);
    }

    /**
     * Returns the extension this pipeline belongs to.
     * 
     * @return the extension this pipeline belongs to
     */
    public final E extension() {
        return extension;
    }

    /** Invoked by the runtime immediately after the pipeline has been constructed. */
    protected void onInitialize() {}

    /**
     * If this pipeline was {@link #spawn(ExtensionWireletList) spawned} from an existing pipeline, returns the pipeline,
     * otherwise returns empty.
     * 
     * @return any pipeline this pipeline was spawned from
     */
    public final Optional<P> previous() {
        return previous;
    }

    /**
     * Spawns a new pipeline from this pipeline. This method is invoked by the runtime whenever
     * 
     * @param wirelets
     *            the wirelets that was used
     * @return the new pipeline
     */
    protected abstract P spawn(ExtensionWireletList<W> wirelets);

    /**
     * Returns a list of the wirelets this pipeline contains.
     * 
     * @return a list of the wirelets this pipeline contains
     */
    public final ExtensionWireletList<W> wirelets() {
        return wirelets;
    }
}
// Two strategies. Either clone all the contents.
// Or recursively call back into parent pipeline
// protected abstract T split();

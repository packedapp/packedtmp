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
// Kan sagtens lave alle metoder public....

// Er en pipeline mere end en pipeline!!!
// Er den ogsaa noden...???

// Drop <T> + split()...

// Vi skal mirror alle metoder paa node....

// Kan only define one pipeline per extension...
// Fordi, vi kalder build paa den...
// ExtensionOrExtensionNode for T

// Vi bliver noedt til at have en tilknyttning til en extension....
// Fordi vi skal vide hvilken extension vi skal kigge i for at finde
// en WireletPipelineFactory....
public abstract class ExtensionWireletPipeline<E extends Extension, P extends ExtensionWireletPipeline<E, P, W>, W extends ExtensionWirelet<P>> {

    private final E extension;

    private final Optional<P> previous;

    private final WireletListNew<W> wirelets;

    protected ExtensionWireletPipeline(E extension, WireletListNew<W> wirelets) {
        this.extension = requireNonNull(extension, "extension is null");
        this.wirelets = requireNonNull(wirelets, "wirelets is null");
        this.previous = Optional.empty();
    }

    protected ExtensionWireletPipeline(P from, WireletListNew<W> wirelets) {
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

    /**
     * Returns any previous pipeline this pipeline is a part of. This is empty unless we are generating from an image.
     * 
     * @return any previous pipeline this pipeline is a part of
     */
    public final Optional<P> previous() {
        return previous;
    }

    protected abstract P spawn(WireletListNew<W> wirelets);

    /**
     * Returns a list of wirelets this pipeline contains.
     * 
     * @return a list of wirelets this pipeline contains
     */
    public final WireletListNew<W> wirelets() {
        return wirelets;
    }

}
// Two strategies. Either clone all the contents.
// Or recursively call back into parent pipeline
// protected abstract T split();

// Ideen er lidt at tage en props.addPipeline(MyPipeline.class, e-> new MyPip(e.mode), Order.SECOND);
// Vi skal ihvertfald draenes af alle annoteringer. Kun paa selve wireletten...
// Alternativet, hvis vi kun har 2-3 vaerdier bare at have 3 metoder i props
// addPipelineLast, addPipelineFirst, ...
// public enum Order {
// FIRST, SECOND, LAST;
// }
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
package packed.internal.container.extension.w2;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;

/**
 *
 */
// Skal vi tage extension context istedet for????
// Ikke saa let at teste. Men vi kan hive informationer ud om containeren..
// Alternativt, en special version af WireletListNew... som embedder ting...

public abstract class XtensionPipeline<E extends Extension, P extends XtensionPipeline<E, P, W>, W extends Wirelet> {

    private final E extension;

    private final Optional<P> previous;

    private final WireletListNew<W> wirelets;

    protected XtensionPipeline(E extension, WireletListNew<W> wirelets) {
        this.extension = requireNonNull(extension, "extension is null");
        this.wirelets = requireNonNull(wirelets, "wirelets is null");
        this.previous = Optional.empty();
    }

    protected XtensionPipeline(P from, WireletListNew<W> wirelets) {
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

// removeAll(CannotBeUsedOnImageWirelet.class,
// if (!previous.isEmpty()) <- on image
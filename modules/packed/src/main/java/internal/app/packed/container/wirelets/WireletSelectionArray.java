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
package internal.app.packed.container.wirelets;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import internal.app.packed.ValueBased;

/**
 *
 */
@ValueBased
public final class WireletSelectionArray<W extends Wirelet> implements WireletSelection<W> {

    /** An empty selection. */
    public static final WireletSelectionArray<?> EMPTY = new WireletSelectionArray<>(List.of());

    // Refactor to array once API is final
    private final List<W> wirelets;

    WireletSelectionArray(List<W> wirelets) {
        this.wirelets = wirelets;
    }


    public static <W extends Wirelet> WireletSelectionArray<W> of(List<W> wirelets) {
        return new WireletSelectionArray<>(wirelets);
    }


    public static <W extends Wirelet> WireletSelectionArray<W> of(W[] wirelets) {
        return new WireletSelectionArray<>(List.of(wirelets));
    }

    public static <W extends Wirelet> WireletSelectionArray<W> ofTrusted(W[] wirelets) {
        return new WireletSelectionArray<>(List.of(wirelets));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<W> first() {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<W> iterator() {
        return wirelets.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<W> last() {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<W> last(Class<? extends W> wireletClass) {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public <E> E lastOrElse(Function<? super W, ? extends E> mapper, E ifEmpty) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public List<W> toList() {
        return wirelets;
    }
}

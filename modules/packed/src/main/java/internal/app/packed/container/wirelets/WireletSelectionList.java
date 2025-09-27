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

import app.packed.container.Wirelet;
import internal.app.packed.ValueBased;

/**
 * An implementation of {@link WireletSelection} based on a list.
 */
@ValueBased
public final class WireletSelectionList<W extends Wirelet> implements Iterable<W> {

    /** An empty selection. */
    public static final WireletSelectionList<?> EMPTY = new WireletSelectionList<>(List.of());

    // Refactor to array once API is final
    private final List<W> wirelets;

    WireletSelectionList(List<W> wirelets) {
        this.wirelets = wirelets;
    }

    public static <W extends Wirelet> WireletSelectionList<W> of(List<W> wirelets) {
        return new WireletSelectionList<>(wirelets);
    }

    public static <W extends Wirelet> WireletSelectionList<W> of(W[] wirelets) {
        return new WireletSelectionList<>(List.of(wirelets));
    }

    public static <W extends Wirelet> WireletSelectionList<W> ofTrusted(W[] wirelets) {
        return new WireletSelectionList<>(List.of(wirelets));
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<W> iterator() {
        return wirelets.iterator();
    }
}

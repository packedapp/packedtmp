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
package app.packed.container;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import internal.app.packed.container.WireletSelectionArray;

/**
 *
 * <p>
 * The framework provides no way to guard against use of multiple wirelets of the same type. This is easier done on the
 * wirelet consumer side (this selection)
 */
@SuppressWarnings("rawtypes")
public sealed interface WireletSelection<W extends Wirelet> extends Iterable<W> permits WireletSelectionArray {

    Optional<W> first();

    boolean isEmpty();

    Optional<W> last();

    // IDK
    Optional<W> last(Class<? extends W> wireletClass);

    <E> E lastOrElse(Function<? super W, ? extends E> mapper, E ifEmpty);

    int size();

    List<W> toList();

    /**
     * Returns an empty selection of wirelets.
     *
     * @param <W>
     *            the type of wirelets in the selection
     * @return an empty wirelet selection
     */
    static <W extends Wirelet> WireletSelection<W> of() {
        @SuppressWarnings("unchecked")
        WireletSelection<W> ws = (WireletSelection<W>) WireletSelectionArray.EMPTY;
        return ws;
    }

    /**
     * This method is mainly used for testing purposes.
     *
     * @param <W>
     *            the type of wirelets in the selection
     * @param wireletClass
     *            the type of wirelets in the selection
     * @param wirelets
     *            the wirelets to include in the selection if they are assignable to the specified {@code wireletClass}.
     * @return the selection
     */
    @SafeVarargs
    static <W extends Wirelet> WireletSelection<W> of(W... wirelets) {
        wirelets = wirelets.clone();
        // check for nulls, copy array;
        throw new UnsupportedOperationException();
    }
}

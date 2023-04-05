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

import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.BindingTypeHook;
import internal.app.packed.container.WireletSelectionArray;

/**
 * A selection of wirelets of a specific type (W).
 * <p>
 * The framework provides no way to guard against use of multiple wirelets of the same type. This is easier done on the
 * wirelet consumer side (this selection)
 */
@SuppressWarnings("rawtypes")
@BindingTypeHook(extension = BaseExtension.class)
public sealed interface WireletSelection<W extends Wirelet> extends Iterable<W> permits WireletSelectionArray {

    /** {@return the first wirelet in this selection or empty {@code Optional}, if no wirelets are present} */
    Optional<W> first();

    /** {@return whether or not this selection contains any wirelets.} */
    boolean isEmpty();

    /** {@return the last wirelet in this selection or empty {@code Optional}, if no wirelets are present} */
    Optional<W> last();

    Optional<W> last(Class<? extends W> wireletClass);

    <E> E lastOrElse(Function<? super W, ? extends E> mapper, E ifEmpty);

    /** {@return the number of wirelets in this selection} */
    int size();

    /** {@return the wirelets in a list} */
    List<W> toList();

    /**
     * Returns an empty selection of wirelets.
     *
     * @param <W>
     *            the type of wirelets in the selection
     * @return an empty selection of wirelets
     */
    static <W extends Wirelet> WireletSelection<W> of() {
        @SuppressWarnings("unchecked")
        WireletSelection<W> ws = (WireletSelection<W>) WireletSelectionArray.EMPTY;
        return ws;
    }

    /**
     * Returns a selection of the specified wirelets.
     * <p>
     * This method is mainly used for testing purposes.
     *
     * @param <W>
     *            the type of wirelets in the selection
     * @param wirelets
     *            the wirelets to include in the selection
     * @return the wirelet selection
     */
    @SafeVarargs
    static <W extends Wirelet> WireletSelection<W> of(W... wirelets) {
        wirelets = wirelets.clone();
        // check for nulls, copy array;
        throw new UnsupportedOperationException();
    }
}

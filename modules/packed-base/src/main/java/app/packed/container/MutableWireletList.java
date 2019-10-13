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

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 */
// Maaske det her skal vaere en WireletList istedet for.....

// Det case for mutable... Vi sharer ikke laengere en liste... Saa folk kan ikke aendre

// Kunne ogsaa starte med at WireletList -> WireletList<W>
public final class MutableWireletList<W extends Wirelet> implements Iterable<W> {

    private List<W> list;

    public MutableWireletList(List<W> list) {
        this.list = requireNonNull(list);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<W> iterator() {
        return list.iterator();
    }

    /**
     * @param filter
     *            a predicate which returns {@code true} for wirelets to be removed
     * @param action
     *            an action to be performed on each removed element
     * @return whether or not any wirelets was removed
     */
    public boolean removeIf(Predicate<? super W> filter, Consumer<? super W> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return list.toString();
    }
}

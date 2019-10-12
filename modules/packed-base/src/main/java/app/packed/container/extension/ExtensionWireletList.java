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

import java.util.Iterator;
import java.util.List;

import app.packed.container.Wirelet;

/**
 *
 */
// Maaske det her skal vaere en WireletList istedet for.....
public final class ExtensionWireletList<W extends Wirelet> implements Iterable<W> {

    private List<W> list;

    public ExtensionWireletList(List<W> list) {
        this.list = requireNonNull(list);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<W> iterator() {
        return list.iterator();
    }
}

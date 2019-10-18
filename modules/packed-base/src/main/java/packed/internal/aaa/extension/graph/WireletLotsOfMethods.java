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
package packed.internal.aaa.extension.graph;

import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.packed.container.Wirelet;
import app.packed.util.Nullable;

/**
 *
 */
class WireletLotsOfMethods<W extends Wirelet> {
    public final int FROM_HEAD = 1;
    public final int FROM_TAIL = 2;
    public final int REMOVE_PROCESSED = 4;
    public final int REMOVE_MATCHED = 8;
    public final int PROCESS_ONE = 16;

    ArrayDeque<W> wirelets;

    public final void process(Consumer<? super W> action) {
        process(action, FROM_HEAD);
    }

    public final void process(Consumer<? super W> action, int options) {
        throw new UnsupportedOperationException();
    }

    public final void removeAll(Predicate<? super W> predicate, Consumer<? super W> action) {
        throw new UnsupportedOperationException();
    }

    public final <T extends W> void removeAll(Class<T> type, Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public final W removeFirst() {
        return wirelets.removeFirst();
    }

    @Nullable
    public final <T extends W> T removeFirst(Predicate<? super W> predicate) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public final <T extends W> T removeFirst(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public final <T extends W> T removeFirst(Class<T> type, Predicate<? super T> predicate) {
        throw new UnsupportedOperationException();
    }
}

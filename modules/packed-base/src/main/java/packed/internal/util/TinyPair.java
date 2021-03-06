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
package packed.internal.util;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;

/**
 *
 */
public record TinyPair<E1, E2> (E1 element1, E2 element2, @Nullable TinyPair<E1, E2> next) {

    public TinyPair(E1 element1, E2 element2, @Nullable TinyPair<E1, E2> next) {
        this.element1 = requireNonNull(element1);
        this.element2 = requireNonNull(element2);
        this.next = next;
    }
}

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
package internal.app.packed.util.handlers;

import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 */
// Think I want to go back to old design with interface and implementatino and skip all the method handle shit
public abstract class NewHandlers {

    static class CachedSupplier<T> implements Supplier<T> {

        private final Supplier<T> originalSupplier;
        private T cachedValue;

        public CachedSupplier(Supplier<T> originalSupplier) {
            this.originalSupplier = Objects.requireNonNull(originalSupplier, "Supplier cannot be null");
        }

        @Override
        public T get() {
            T c = cachedValue;
            if (c == null) {
                c = originalSupplier.get();
                if (cachedValue == null) {
                    throw new IllegalStateException("Wrapped supplier returned null, which is not allowed.");
                }
            }
            return cachedValue = c;
        }
    }
}

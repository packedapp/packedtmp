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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import app.packed.util.Key;

/**
 *
 */
public final class ContractBuilder {

    public Services services() {
        return new Services();
    }

    @SuppressWarnings("unchecked")
    <T> T with(Class<T> type) {
        requireNonNull(type, "type is null");
        if (type == Services.class) {
            return (T) services();
        }
        throw new UnsupportedOperationException();
    }

    // Spoergsmaalet er om vi skal kunne fjerne ting....
    // F.eks. et requirement, det er jo primaert tiltaenkt hvis vi begynder noget med versioner....
    // Noget andet
    public final class Services {
        Services() {}

        public Services requires(Class<?> key) {
            return requires(Key.of(key));
        }

        public Services requires(Key<?> key) {
            return this;
        }
    }
}

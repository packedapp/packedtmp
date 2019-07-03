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
package app.packed.feature;

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;

/**
 *
 */
public final class FeatureMap {
    private final IdentityHashMap<FeatureKey<?>, Object> features = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(FeatureKey<T> key) {
        return (T) features.get(key);
    }

    public <T> void set(FeatureKey<T> key, T value) {
        features.put(key, requireNonNull(value));
    }
}

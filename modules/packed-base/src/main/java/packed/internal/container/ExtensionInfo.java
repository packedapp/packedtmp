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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import app.packed.container.Extension;

/**
 *
 */
// Raekkefoelge af installeret extensions....
// Maaske bliver vi noedt til at have @UsesExtension..
// Saa vi kan sige X extension skal koeres foerend Y extension
public final class ExtensionInfo<T> {

    /** The cache of values. */
    private static final ClassValue<ExtensionInfo<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected ExtensionInfo<?> computeValue(Class<?> type) {
            return new ExtensionInfo(type);
        }
    };

    final Class<? extends Extension<?>> type;

    private ExtensionInfo(Class<? extends Extension<?>> type) {
        this.type = requireNonNull(type);
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    public T newInstance() {
        try {
            return (T) type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate extension", e);
        }

    }

    @SuppressWarnings("unchecked")
    public static <T extends Extension<T>> T newInstance(Class<T> extensionType) {
        return (T) CACHE.get(extensionType).newInstance();
    }
}

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

import app.packed.container.AnyBundle;

/**
 *
 */
final class BundleClassCache {

    /** A cache of values. */
    private static final ClassValue<BundleClassCache> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected BundleClassCache computeValue(Class<?> type) {
            return new BundleClassCache((Class) type);
        }
    };

    /** The type of extension. */
    private final Class<? extends AnyBundle> type;

    /** The default name of the bundle */
    private volatile String defaultName;

    String defaultName() {
        String d = defaultName;
        if (d == null) {
            d = defaultName = type.getSimpleName() + "?";
        }
        return d;
    }

    /**
     * Creates a new extension class cache.
     * 
     * @param type
     *            the extension type
     */
    private BundleClassCache(Class<? extends AnyBundle> type) {
        this.type = requireNonNull(type);
    }

    static BundleClassCache of(Class<? extends AnyBundle> c) {
        return CACHE.get(c);
    }
}

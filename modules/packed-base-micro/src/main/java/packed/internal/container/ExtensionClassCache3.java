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

import java.util.function.Supplier;

import app.packed.extension.Extension;

/**
 * A cache of {@link Extension} implementations. Is mainly used for instantiating new instances of extensions.
 */
// Raekkefoelge af installeret extensions....
// Maaske bliver vi noedt til at have @UsesExtension..
// Saa vi kan sige X extension skal koeres foerend Y extension
final class ExtensionClassCache3<T> {

    /** A cache of values. */
    private static final ClassValue<Supplier<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected Supplier<?> computeValue(Class<?> type) {
            return () -> new ExtensionMicro.MyExtension();
        }
    };

    /**
     * Creates a new extension of the specified type.
     * 
     * @param <T>
     *            the type of extension
     * @param extensionClass
     *            the type of extension
     * @return a new instance of the extension
     */
    @SuppressWarnings("unchecked")
    static <T extends Extension> T newInstance(Class<T> extensionClass) {
        // Time goes from around 1000 ns to 10 ns when we cache
        return (T) CACHE.get(extensionClass).get();
    }
}

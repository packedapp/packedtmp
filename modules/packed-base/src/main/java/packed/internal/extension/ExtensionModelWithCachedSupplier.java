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
package packed.internal.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.function.Supplier;

import app.packed.extension.Extension;
import app.packed.util.IllegalAccessRuntimeException;
import packed.internal.util.StringFormatter;

/**
 * A cache of {@link Extension} implementations. Is mainly used for instantiating new instances of extensions.
 */
// Raekkefoelge af installeret extensions....
// Maaske bliver vi noedt til at have @UsesExtension..
// Saa vi kan sige X extension skal koeres foerend Y extension
final class ExtensionModelWithCachedSupplier<T> {

    /** A cache of values. */
    private static final ClassValue<Supplier<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Supplier<?> computeValue(Class<?> type) {
            return new ExtensionModelWithCachedSupplier(type).s;
        }
    };

    /** The method handle used to create a new instance of the extension. */
    private final Supplier<T> s;

    /** The type of extension. */
    final Class<? extends Extension> type;

    /**
     * Creates a new extension class cache.
     * 
     * @param type
     *            the extension type
     */
    private ExtensionModelWithCachedSupplier(Class<? extends Extension> type) {
        this.type = requireNonNull(type);
        /// TODO Check not abstract
        Constructor<?> constructor;
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The extension " + StringFormatter.format(type) + " must have a no-argument constructor.");
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            lookup = MethodHandles.privateLookupIn(type, lookup);
            MethodHandle mh = lookup.unreflectConstructor(constructor);

            MethodType methodType = MethodType.methodType(Object.class);
            MethodType invokedType = MethodType.methodType(Supplier.class);

            CallSite site = LambdaMetafactory.metafactory(lookup, "get", invokedType, methodType, mh, methodType);
            MethodHandle factory = site.getTarget();
            s = (Supplier<T>) factory.invoke();
        } catch (Throwable e) {
            throw new IllegalAccessRuntimeException("In order to use the extension " + StringFormatter.format(type) + ", the module '"
                    + type.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
        }

    }

    /**
     * Creates a new extension of the specified type.
     * 
     * @param <T>
     *            the type of extension
     * @param extensionType
     *            the type of extension
     * @return a new instance of the extension
     */
    @SuppressWarnings("unchecked")
    static <T extends Extension> T newInstance(Class<T> extensionType) {
        // Time goes from around 1000 ns to 10 ns when we cache
        return (T) CACHE.get(extensionType).get();
    }
}

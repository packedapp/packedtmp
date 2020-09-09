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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import app.packed.base.TypeLiteral;
import app.packed.inject.Factory1;
import packed.internal.inject.resolvable.ServiceDependency;
import packed.internal.util.LookupUtil;

/** An internal factory for {@link Factory1}. */
public final class Factory1FactoryHandle<T, R> extends FactoryHandle<R> {

    /** A method handle for {@link Function#apply(Object)}. */
    private static final MethodHandle APPLY = LookupUtil.mhVirtualPublic(Function.class, "apply", Object.class, Object.class);

    /** The function that creates the actual objects. */
    private final Function<? super T, ? extends R> function;

    private Factory1FactoryHandle(TypeLiteral<R> type, Function<? super T, ? extends R> function) {
        super(type);
        this.function = requireNonNull(function, "function is null");
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle() {
        return APPLY.bindTo(function);
    }

    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    private static final ClassValue<Entry<TypeLiteral<?>, List<ServiceDependency>>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Entry<TypeLiteral<?>, List<ServiceDependency>> computeValue(Class<?> type) {
            return new SimpleImmutableEntry<>(TypeLiteral.fromTypeVariable((Class) type, BaseFactory.class, 0),
                    ServiceDependency.fromTypeVariables((Class) type, Factory1.class, 0));
        }
    };

    /**
     * Creates a new factory support instance from an implementation of this class and a function.
     * 
     * @param implementation
     *            the class extending this class
     * @param function
     *            the function used for creating new values
     * @return a new factory support instance
     */
    @SuppressWarnings("unchecked")
    public static <T, R> FactorySupport<R> create(Class<?> implementation, Function<?, ? extends T> function) {
        Entry<TypeLiteral<?>, List<ServiceDependency>> fs = CACHE.get(implementation);
        return new FactorySupport<>(new Factory1FactoryHandle<>((TypeLiteral<R>) fs.getKey(), (Function<? super T, ? extends R>) function), fs.getValue());
    }
}
//
// @SuppressWarnings("unchecked")
// @Override
// @Nullable
// public R invoke(Object[] params) {
// T t = (T) params[0];
// R instance = function.apply(t);
// if (!returnTypeRaw().isInstance(instance)) {
// throw new InjectionException(
// "The Function '" + format(function.getClass()) + "' used when creating a Factory1 instance was expected to produce
// instances of '"
// + format(returnTypeRaw()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
// }
// return instance;
// }

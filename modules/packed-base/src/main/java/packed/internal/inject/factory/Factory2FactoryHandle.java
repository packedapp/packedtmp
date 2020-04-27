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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import app.packed.base.TypeLiteral;
import app.packed.inject.Factory2;
import packed.internal.inject.ServiceDependency;
import packed.internal.util.LookupUtil;

/** An internal factory for {@link Factory2}. */
public class Factory2FactoryHandle<T, U, R> extends FactoryHandle<R> {

    /** A method handle for {@link BiFunction#apply(Object, Object)}. */
    private static final MethodHandle APPLY = LookupUtil.findVirtualEIIE(MethodHandles.lookup(), BiFunction.class, "apply",
            MethodType.methodType(Object.class, Object.class, Object.class));

    /** The function responsible for creating the actual objects. */
    private final BiFunction<? super T, ? super U, ? extends R> function;

    private Factory2FactoryHandle(TypeLiteral<R> typeLiteral, BiFunction<? super T, ? super U, ? extends R> function) {
        super(typeLiteral);
        this.function = requireNonNull(function);
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
                    ServiceDependency.fromTypeVariables((Class) type, Factory2.class, 0, 1));
        }
    };

    /**
     * Creates a new factory support instance from an implementation of this class and a (bi) function.
     * 
     * @param implementation
     *            the class extending this class
     * @param function
     *            the (bi) function used for creating new values
     * @return a new factory support instance
     */
    @SuppressWarnings("unchecked")
    public static <T, U, R> FactorySupport<R> create(Class<?> implementation, BiFunction<?, ?, ? extends R> function) {
        Entry<TypeLiteral<?>, List<ServiceDependency>> fs = CACHE.get(implementation);
        return new FactorySupport<>(new Factory2FactoryHandle<>((TypeLiteral<R>) fs.getKey(), (BiFunction<? super T, ? super U, ? extends R>) function),
                fs.getValue());
    }
}

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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import packed.internal.inject.DependencyDescriptor;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 * A {@link Factory} type that takes two dependencies and uses a {@link BiFunction} to create new instances. The input
 * to the bi-function being the two dependencies.
 * 
 * @see Factory0
 * @see Factory1
 */
public abstract class Factory2<T, U, R> extends Factory<R> {

    /** A method handle for invoking {@link #create(BiFunction, Class, Object, Object)}. */
    private static final MethodHandle CREATE = LookupUtil.lookupStatic(MethodHandles.lookup(), "create", Object.class, BiFunction.class, Class.class,
            Object.class, Object.class);

    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    private static final ClassValue<List<DependencyDescriptor>> TYPE_VARIABLE_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected List<DependencyDescriptor> computeValue(Class<?> type) {
            return DependencyDescriptor.fromTypeVariables((Class) type, Factory2.class, 0, 1);
        }
    };

    /** The dependencies of this factory, extracted from the type variables of the subclass. */
    private final List<DependencyDescriptor> dependencies;

    /** The method handle responsible for providing the actual values. */
    private final MethodHandle methodHandle;

    /**
     * Creates a new factory, that uses the specified function to provide instances.
     *
     * @param function
     *            the function that provide instances. The function should never return null, but should instead throw a
     *            relevant exception if unable to provide a value
     * @throws FactoryException
     *             if any of type variables could not be determined. Or if R does not represent a valid key, for example,
     *             {@link Optional}
     */
    protected Factory2(BiFunction<? super T, ? super U, ? extends R> function) {
        requireNonNull(function, "function is null");
        this.dependencies = TYPE_VARIABLE_CACHE.get(getClass());
        MethodHandle mh = CREATE.bindTo(function).bindTo(rawType()); // (Function, Class, Object, Object)Object -> (Object, Object)Object
        this.methodHandle = MethodHandleUtil.castReturnType(mh, rawType()); // (Object, Object)Object -> (Object, Object)R
    }

    /** {@inheritDoc} */
    @Override
    List<DependencyDescriptor> dependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    MethodHandle toMethodHandle(Lookup ignore) {
        return methodHandle;
    }

    /**
     * Supplies a value.
     * 
     * @param <T>
     *            the type of value supplied
     * @param function
     *            the function that supplies the actual value
     * @param expectedType
     *            the type we expect the supplier to return
     * @param obj1
     *            the 1st argument to the function
     * @param obj2
     *            the 2nd argument to the function
     * @return the value that was supplied by the specified supplier
     * @throws FactoryException
     *             if the created value is null or not assignable to the raw type of the factory
     */
    @SuppressWarnings("unused") // only invoked via #CREATE
    private static <T> T create(BiFunction<Object, Object, ? extends T> function, Class<?> expectedType, Object obj1, Object obj2) {
        T value = function.apply(obj1, obj2);
        checkReturnValue(expectedType, value, function);
        return value;
    }

}

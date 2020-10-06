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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.methodhandle.LookupUtil;
import packed.internal.methodhandle.MethodHandleUtil;

/**
 * A {@link Factory} type that uses a {@link Supplier} to provide instances.
 * <p>
 * This class is typically used like this:
 * 
 * <pre> {@code Factory<Long> f = new Factory0<>(System::currentTimeMillis) {};}</pre>
 * <p>
 * In this example we create a new class that extends Factory0 is order to capture information about the suppliers type
 * variable (in this case {@code Long}). Thereby circumventing the limitations of Java's type system for retaining type
 * information at runtime.
 * <p>
 * Qualifier annotations can be used if they have {@link ElementType#TYPE_USE} in their {@link Target}:
 * 
 * <pre> {@code Factory<Long> f = new Factory0<@SomeQualifier Long>(() -> 1L) {};}</pre>
 * 
 * @param <R>
 *            the type of instances this factory provide
 * @see Factory1
 * @see Factory2
 */
public abstract class Factory0<R> extends Factory<R> {

    /** A method handle for invoking {@link #create(Supplier, Class)}. */
    private static final MethodHandle CREATE = LookupUtil.lookupStatic(MethodHandles.lookup(), "create", Object.class, Supplier.class, Class.class);

    /** The method handle responsible for providing the actual values. Eagerly created. */
    private final MethodHandle methodHandle;

    /**
     * Creates a new factory, that use the specified supplier to provide values.
     *
     * @param supplier
     *            the supplier that will provide the actual values. The supplier should never return null, but should
     *            instead throw a relevant exception if unable to provide a value
     * @throws FactoryException
     *             if the type variable R could not be determined. Or if R does not represent a valid key, for example,
     *             {@link Optional}
     */
    protected Factory0(Supplier<? extends R> supplier) {
        requireNonNull(supplier, "supplier is null");
        MethodHandle mh = CREATE.bindTo(supplier).bindTo(rawType()); // (Supplier, Class)Object -> ()Object
        this.methodHandle = MethodHandleUtil.castReturnType(mh, rawType()); // ()Object -> ()R
    }

    /** {@inheritDoc} */
    @Override
    List<DependencyDescriptor> dependencies() {
        return List.of();
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
     * @param supplier
     *            the supplier that supplies the actual value
     * @param expectedType
     *            the type we expect the supplier to return
     * @return the value that was supplied by the specified supplier
     * @throws FactoryException
     *             if the created value is null or not of an expected type
     */
    @SuppressWarnings("unused") // only invoked via #CREATE
    private static <T> T create(Supplier<? extends T> supplier, Class<?> expectedType) {
        T value = supplier.get();
        if (!expectedType.isInstance(value)) {
            if (value == null) {
                // NPE???
                throw new FactoryException("The supplier '" + supplier + "' must not return null");
            } else {
                throw new FactoryException("The supplier '" + supplier + "' was expected to return instances of type " + expectedType.getName()
                        + " but returned a " + value.getClass().getName() + " instance");
            }
        }
        return value;
    }
}

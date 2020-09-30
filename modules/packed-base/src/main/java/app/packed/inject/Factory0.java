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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.function.Supplier;

import packed.internal.inject.factory.BaseFactory;

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
public abstract class Factory0<R> extends BaseFactory<R> {

    /**
     * Creates a new factory, that uses the specified supplier to provide instances.
     *
     * @param supplier
     *            the supplier that provide instances. The supplier should never return null, but should instead throw a
     *            relevant exception if unable to provide a value
     * @throws FactoryDefinitionException
     *             if the type variable R could not be determined. Or if R does not represent a valid key, for example,
     *             {@link Optional}
     */
    protected Factory0(Supplier<? extends R> supplier) {
        super(supplier);
    }
}

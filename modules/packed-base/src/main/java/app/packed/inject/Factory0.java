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

import java.util.Optional;
import java.util.function.Supplier;

import app.packed.util.InvalidDeclarationException;

/**
 * A {@link Factory} type that provides an easy way to use a {@link Supplier} to dynamically provide new instances,
 * thereby circumventing the limitations of Java's type system.
 * <p>
 * Is typically used like this:
 *
 * <pre> {@code
 * Factory<Long> f = new Factory0<>(System::currentTimeMillis) {};}</pre>
 * <p>
 * Note: that we create a new class inheriting from Factory0 is order to capture information about the type variable (in
 * this case {@code Long}).
 * 
 * @param <R>
 *            the type of objects this factory constructs
 * @see Factory1
 * @see Factory2
 */
public abstract class Factory0<R> extends Factory<R> {

    /**
     * Creates a new factory, that uses the specified supplier to create new instances.
     *
     * @param supplier
     *            the supplier to use for creating new instances, the supplier should never return null
     * @throws InvalidDeclarationException
     *             if the type variable R could not be determined. Or if R does not represent a valid key, for example,
     *             {@link Optional}
     */
    protected Factory0(Supplier<? extends R> supplier) {
        super(supplier);
    }
}

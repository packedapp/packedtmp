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
package app.packed.util;

import java.lang.reflect.Constructor;

import app.packed.inject.Factory;
import app.packed.inject.TypeLiteral;
import packed.util.descriptor.InternalConstructorDescriptor;

/**
 * A constructor descriptor.
 * <p>
 * Unlike the {@link Constructor} class, this interface contains no mutable operations, so it can be freely shared.
 */
public interface ConstructorDescriptor<T> extends ExecutableDescriptor {

    /**
     * Returns a new constructor.
     *
     * @return a new constructor
     */
    Constructor<?> newConstructor();

    /**
     * Returns a new factory from this constructor. Taking all of this constructor's parameters as dependencies.
     *
     * @throws IllegalArgumentException
     *             if some of the parameters are not valid.
     *
     * @return a new factory from this constructor
     */
    default Factory<T> toFactory() {
        throw new UnsupportedOperationException();
    }

    static <T> ConstructorDescriptor<T> of(Constructor<T> constructor) {
        return InternalConstructorDescriptor.of(constructor);
    }

    static <T> ConstructorDescriptor<T> of(Class<T> declaringClass, Class<?>... parameterTypes) {
        throw new UnsupportedOperationException();
    }

    static <T> ConstructorDescriptor<T> of(TypeLiteral<T> declaringClass, Class<?>... parameterTypes) {
        throw new UnsupportedOperationException();
    }
}

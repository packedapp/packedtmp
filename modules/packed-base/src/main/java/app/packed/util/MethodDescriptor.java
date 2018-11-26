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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.inject.TypeLiteral;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * A method descriptor.
 * <p>
 * Unlike the {@link Method} class, this interface contains no mutable operations, so it can be freely shared.
 */
public interface MethodDescriptor extends ExecutableDescriptor {

    /**
     * Returns a {@code Class} object that represents the formal return type of this method .
     *
     * @return the return type of this method
     * @see Method#getReturnType()
     */
    Class<?> getReturnType();

    /**
     * Returns whether or not this method is a static method.
     *
     * @return whether or not this method is a static method
     * @see Modifier#isStatic(int)
     */
    default boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    /**
     * Returns a new method from this descriptor.
     *
     * @return a new method from this descriptor
     */
    Method newMethod();

    /**
     * Returns a type literal that identifies the generic type return type of the method.
     *
     * @return a type literal that identifies the generic type return type of the method
     * @see Method#getGenericReturnType()
     */
    TypeLiteral<?> getReturnTypeLiteral();

    public static MethodDescriptor of(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a new method descriptor for the specified method.
     *
     * @param method
     *            the method to return a descriptor from
     * @return the new method descriptor
     */
    public static MethodDescriptor of(Method method) {
        return InternalMethodDescriptor.of(method);
    }
}

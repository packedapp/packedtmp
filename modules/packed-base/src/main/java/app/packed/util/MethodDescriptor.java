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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * Provides information about a method, such as its name, parameters, annotations. Unlike {@link Method} this class is
 * immutable, and can be be freely shared.
 */
// Refac using
// https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/lang/model/element/ExecutableElement.html
public interface MethodDescriptor extends ExecutableDescriptor {

    /**
     * Returns whether or not this method is a static method.
     *
     * @return whether or not this method is a static method
     * @see Modifier#isStatic(int)
     */
    boolean isStatic();

    /**
     * Returns a new method from this descriptor.
     *
     * @return a new method from this descriptor
     */
    Method newMethod();

    /**
     * Returns a {@code Class} object that represents the formal return type of this method .
     *
     * @return the return type of this method
     * @see Method#getReturnType()
     */
    Class<?> returnType();

    /**
     * Returns a type literal that identifies the generic type return type of the method.
     *
     * @return a type literal that identifies the generic type return type of the method
     * @see Method#getGenericReturnType()
     */
    TypeLiteral<?> returnTypeLiteral();

    /**
     * Produces a method handle for the underlying method.
     * 
     * @param lookup
     *            the lookup object
     * @param specialCaller
     *            the class nominally calling the method
     * @return a method handle which can invoke the reflected method
     * @throws IllegalAccessException
     *             if access checking fails, or if the method is {@code static}, or if the method's variable arity modifier
     *             bit is set and {@code asVarargsCollector} fails
     * @see Lookup#unreflectSpecial(Method, Class)
     */
    MethodHandle unreflectSpecial(MethodHandles.Lookup lookup, Class<?> specialCaller) throws IllegalAccessException;

    static MethodDescriptor of(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a new method descriptor from the specified method.
     *
     * @param method
     *            the method to return a descriptor from
     * @return the new method descriptor
     */
    static MethodDescriptor of(Method method) {
        return InternalMethodDescriptor.of(method);
    }
}

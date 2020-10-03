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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import app.packed.base.Key;
import app.packed.base.TypeLiteral;
import app.packed.inject.Factory;
import app.packed.inject.Inject;
import app.packed.introspection.ConstructorDescriptor;
import app.packed.introspection.MethodDescriptor;

/**
 *
 */

//
///**
//* Returns a new bindable factory.
//*
//* @return a new bindable factory
//*/
//public BindableFactory<T> bindable() {
//// bindable, newBindable, toBindable()
//return new BindableFactory<>(factory);
//}

//Finde metoder paa statisk vs instance....
//Hvis vi kun tillader @Inject paa de statiske, bliver algoritmerne lidt for forskellige..

class NewFactoryMethods {

    // either one annotated with inject, or just one method, for example lambda..

    /**
     * This method will attempt to find exactly one static method annotated with inject. Either because there is only one
     * method that returns returnType or because there is one annotated with {@link Inject}
     *
     * @param implementation
     *            the implementation
     * @param returnType
     *            the
     * @return a factory wrapping the st
     * @throws IllegalArgumentException
     *             if there was not exactly one static method annotated with {@link Inject} or if the return type of the
     *             method did not match the specified return type
     */
    public static <T> Factory<T> findMethod(Class<?> implementation, Class<T> returnType) {
        return findMethod(implementation, TypeLiteral.of(returnType));
    }

    public static <T> Factory<T> findMethod(Class<?> implementation, TypeLiteral<T> returnType) {
        // I think scan from exactly one 1 inject method
        throw new UnsupportedOperationException();
    }

    // Will look for a single static method annotated with Inject...
    public static <T> Factory<T> findMethod(Class<T> type) {
        return findMethod(requireNonNull(type), TypeLiteral.of(type));
    }

    // Ideen er lidt at der kun maa vaere en metode med det navn...
    // Ellers fejler vi
    public static <T> Factory<T> findNamed(Class<T> key, String name) {
        return findMethod(requireNonNull(key), TypeLiteral.of(key));
    }

    /**
     * Creates a new factory that uses the specified constructor to create new instances.
     *
     * @param constructor
     *            the constructor used for creating new instances
     * @return the new factory
     */
    public static <T> Factory<T> fromConstructor(Constructor<? extends T> constructor) {
        throw new UnsupportedOperationException();
        // MirrorOfClass<T> mirror = (MirrorOfClass<T>) MirrorOfClass.fromImplementation(requireNonNull(constructor,
        // "constructor is null").getDeclaringClass());
        // return InternalExecutableFactory.from(mirror, mirror.constructors().match(constructor.getParameterTypes()));
    }

    /**
     * Creates a new factory that uses the specified constructor to create new instances. Compared to the simpler
     * {@link #fromConstructor(Constructor)} method this method takes a type literal that can be used to create factories
     * with a generic signature:
     *
     * <pre>
     * Factory<List<String>> f = Factory.fromConstructor(ArrayList.class.getConstructor(), new TypeLiteral<List<String>>() {
     * });
     * </pre>
     *
     * @param constructor
     *            the constructor used from creating an instance
     * @param type
     *            a type literal
     * @return the new factory
     * @see #fromConstructor(Constructor)
     */
    public static <T> Factory<T> fromConstructor(Constructor<? extends T> constructor, TypeLiteral<? extends T> type) {
        return fromConstructor(constructor);
    }

    public static <T> Factory<T> fromConstructor(Constructor<?> constructor, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> fromConstructor(ConstructorDescriptor<T> constructor) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a factory by looking at the specified type for a constructor with the specified parameter types. For example,
     * the following example will return a factory that uses {@link String#String(StringBuilder)} to create new instances:
     *
     * <pre>
     * Factory<String> factory = fromConstructor(String.class, StringBuilder.class);
     * </pre>
     *
     * @param constructor
     *            the constructor
     * @param key
     *            the keft to register under
     * @return the new factory
     * @throws IllegalArgumentException
     *             if a constructor with the specified parameter types does not exist on the specified type
     */
    public static <T> Factory<T> fromConstructor(ConstructorDescriptor<T> constructor, Key<T> key) {
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> fromMethod(Class<?> implementation, Class<T> returnType, String name, Class<?>... parameters) {
        return fromMethod(implementation, TypeLiteral.of(requireNonNull(returnType, "returnType is null")), name, parameters);
    }

    // How we skal have
    public static <T> Factory<T> fromMethod(Class<?> implementation, TypeLiteral<T> returnType, String name, Class<?>... parameters) {
        throw new UnsupportedOperationException();
    }

    // new Factory<String>(SomeMethod);
    // How we skal have
    public static <T> Factory<T> fromMethod(Method method, Class<T> returnType) {
        return fromMethod(method, TypeLiteral.of(requireNonNull(returnType, "returnType is null")));
    }

    // Den her sletter evt. Qualifier paa metoden...
    public static <T> Factory<T> fromMethod(Method method, TypeLiteral<T> returnType) {
        requireNonNull(method, "method is null");
        requireNonNull(returnType, "returnType is null");
        // ClassMirror mirror = ClassMirror.fromImplementation(method.getDeclaringClass());
        // return new Factory<T>(new InternalFactory.fromExecutable<T>((Key<T>) mirror.getKey().ofType(returnType), mirror,
        // Map.of(), new MethodMirror(method)));
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> fromMethod(MethodDescriptor method, Class<T> returnType) {
        // Syntes vi skal omnavngive den til
        // fromMethod + fromMethodInstance
        throw new UnsupportedOperationException();
    }

}

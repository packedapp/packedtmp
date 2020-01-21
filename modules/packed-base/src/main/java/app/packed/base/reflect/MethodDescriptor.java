/*
w * Copyright (c) 2008 Kasper Nielsen.
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
package app.packed.base.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.TypeLiteral;
import packed.internal.util.StringFormatter;

/**
 * Provides information about a method, such as its name, parameters, annotations. Unlike {@link Method} this class is
 * immutable, and can be be freely shared.
 * 
 * @apiNote In the future, if the Java language permits, {@link MethodDescriptor} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
// Refac using
// https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/lang/model/element/ExecutableElement.html

// Hmm kan vi lave nogle build steps, hvor vi f.eks. bruger ASM istedet for Java reflection.
// Saaledes at vi undgaar at lave annotations proxies....
// DVS vi har ikke en metode i maven...
public final class MethodDescriptor extends ExecutableDescriptor {

    /** The method that is being mirrored (private to avoid exposing). */
    private final Method method;

    /**
     * Creates a new InternalMethodDescriptor from the specified method.
     *
     * @param method
     *            the method to create a descriptor from
     */
    private MethodDescriptor(Method method) {
        super(requireNonNull(method, "method is null"));
        this.method = method;
    }

    /** {@inheritDoc} */
    @Override
    public String descriptorTypeName() {
        return "method";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable Object obj) {
        // return obj == this || (obj instanceof MethodDescriptor d && d.method.equals(method));
        if (obj == this) {
            return true;
        } else if (obj instanceof MethodDescriptor) {
            return ((MethodDescriptor) obj).method.equals(method);
        }
        return false;
    }

    public Key<?> fromMethodReturnType() {
        return Key.fromMethodReturnType(method);
    }

    /**
     * Returns the generic return type of the method.
     *
     * @return the generic return type of the method
     */
    public Type getGenericReturnType() {
        return method.getGenericReturnType();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return method.getName();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return method.hashCode();
    }

    /**
     * Returns whether or not this method is a static method.
     *
     * @return whether or not this method is a static method
     * @see Modifier#isStatic(int)
     */
    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public boolean overrides(MethodDescriptor supeer) {
        if (methodOverrides(this.method, supeer.method)) {
            if (getName().equals(supeer.getName())) {
                return Arrays.equals(parameterTypes, supeer.parameterTypes);
            }
        }
        return false;
    }

    /**
     * Returns a {@code Class} object that represents the formal return type of this method .
     *
     * @return the return type of this method
     * @see Method#getReturnType()
     */
    public Class<?> returnType() {
        return method.getReturnType();
    }

    /**
     * Returns a type literal that identifies the generic type return type of the method.
     *
     * @return a type literal that identifies the generic type return type of the method
     * @see Method#getGenericReturnType()
     */
    public TypeLiteral<?> returnTypeLiteral() {
        return TypeLiteral.fromMethodReturnType(method);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return StringFormatter.format(method);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle unreflect(Lookup lookup) throws IllegalAccessException {
        requireNonNull(lookup, "lookup is null");
        return lookup.unreflect(method);
    }

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
    public MethodHandle unreflectSpecial(Lookup lookup, Class<?> specialCaller) throws IllegalAccessException {
        return lookup.unreflectSpecial(method, specialCaller);
    }

    /**
     * Returns true if a overrides b. Assumes signatures of a and b are the same and a's declaring class is a subclass of
     * b's declaring class.
     */
    private static boolean methodOverrides(Method sub, Method supeer) {
        int modifiers = supeer.getModifiers();
        if (Modifier.isPrivate(modifiers)) {
            return false;
        }
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)
                || sub.getDeclaringClass().getPackage().equals(supeer.getDeclaringClass().getPackage());
    }

    /**
     * Creates a new descriptor from the specified method.
     *
     * @param method
     *            the method to wrap
     * @return a new method descriptor
     */
    public static MethodDescriptor of(Method method) {
        return new MethodDescriptor(method);
    }

    public final boolean isNullableReturnType() {
        return isAnnotationPresent(Nullable.class);
    }
}
//
// /** {@inheritDoc} */
// @Override
// // TODO hide it
// Executable newExecutable() {
// return newMethod();
// }

/// **
// * Returns a new method from this descriptor.
// *
// * @return a new method from this descriptor
// */
//// TODO hide it
// public Method newMethod() {
// Class<?> declaringClass = method.getDeclaringClass();
// try {
// return declaringClass.getDeclaredMethod(method.getName(), parameterTypes);
// } catch (NoSuchMethodException e) {
// throw new InternalErrorException("method", method, e);// We should never get to here
// }
// }

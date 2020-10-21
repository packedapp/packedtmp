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
package packed.internal.introspection;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import app.packed.base.Nullable;
import packed.internal.util.StringFormatter;

/**
 * Provides information about a method, such as its name, parameters, annotations. Unlike {@link Method} this class is
 * immutable, and can be be freely shared.
 * 
 */
// Refac using
// https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/lang/model/element/ExecutableElement.html

// Hmm kan vi lave nogle build steps, hvor vi f.eks. bruger ASM istedet for Java reflection.
// Saaledes at vi undgaar at lave annotations proxies....
// DVS vi har ikke en metode i maven...
public final class PackedMethodDescriptor extends PackedExecutableDescriptor {

    /** The method that is being mirrored (private to avoid exposing). */
    private final Method method;
    /** The parameter types of the executable. */
    final Class<?>[] parameterTypes;

    /**
     * Creates a new InternalMethodDescriptor from the specified method.
     *
     * @param method
     *            the method to create a descriptor from
     */
    public PackedMethodDescriptor(Method method) {
        super(requireNonNull(method, "method is null"));
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable Object obj) {
        // return obj == this || (obj instanceof MethodDescriptor d && d.method.equals(method));
        if (obj == this) {
            return true;
        } else if (obj instanceof PackedMethodDescriptor) {
            return ((PackedMethodDescriptor) obj).method.equals(method);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return method.hashCode();
    }

    public boolean overrides(PackedMethodDescriptor supeer) {
        PackedMethodDescriptor pmd = supeer;
        if (methodOverrides(this.method, pmd.method)) {
            if (method.getName().equals(supeer.method.getName())) {
                return Arrays.equals(parameterTypes, pmd.parameterTypes);
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return StringFormatter.format(method);
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

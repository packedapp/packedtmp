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
package packed.internal.util.descriptor;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.invokers.ExecutableInvoker;
import packed.internal.invokers.InvokableMember;
import packed.internal.util.InternalErrorException;
import packed.internal.util.StringFormatter;

/** The default implementation of {@link MethodDescriptor}. */
public final class InternalMethodDescriptor extends InternalExecutableDescriptor implements MethodDescriptor {

    /** The method that is being mirrored (private to avoid exposing). */
    private final Method method;

    /**
     * Creates a new InternalMethodDescriptor from the specified method.
     *
     * @param method
     *            the method to create a descriptor from
     */
    private InternalMethodDescriptor(Method method) {
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
        if (obj == this) {
            return true;
        } else if (obj instanceof InternalMethodDescriptor) {
            return ((InternalMethodDescriptor) obj).method.equals(method);
        } else if (obj instanceof MethodDescriptor) {
            return ((MethodDescriptor) obj).newMethod().equals(method);
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
    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    /** {@inheritDoc} */
    @Override
    public TypeLiteral<?> getReturnTypeLiteral() {
        return TypeLiteral.fromMethodReturnType(method);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return method.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public Executable newExecutable() {
        return newMethod();
    }

    /** {@inheritDoc} */
    @Override
    public InvokableMember<?> newInvoker(Lookup lookup) {
        return new ExecutableInvoker<>(this).withLookup(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public Method newMethod() {
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            return declaringClass.getDeclaredMethod(method.getName(), parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new InternalErrorException("method", method, e);// We should never get to here
        }
    }

    public boolean overrides(InternalMethodDescriptor supeer) {
        if (methodOverrides(this.method, supeer.method)) {
            if (getName().equals(supeer.getName())) {
                return Arrays.equals(parameterTypes, supeer.parameterTypes);
            }
        }
        return false;
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
    public static InternalMethodDescriptor of(Method method) {
        return new InternalMethodDescriptor(method);
    }
}

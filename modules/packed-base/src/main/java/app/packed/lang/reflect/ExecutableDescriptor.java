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
package app.packed.lang.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * An executable descriptor.
 * <p>
 * Unlike the {@link Executable} class, this interface contains no mutable operations, so it can be freely shared.
 */
public abstract class ExecutableDescriptor implements Member, AnnotatedElement {

    /** The executable */
    final Executable executable;

    /** An array of the parameter descriptor for this executable */
    private final ParameterDescriptor[] parameters;

    /** The parameter types of the executable. */
    final Class<?>[] parameterTypes;

    /**
     * Creates a new descriptor from the specified executable.
     *
     * @param executable
     *            the executable to mirror
     */
    ExecutableDescriptor(Executable executable) {
        this.executable = executable;
        Parameter[] parameters = executable.getParameters();
        this.parameters = new ParameterDescriptor[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            this.parameters[i] = new ParameterDescriptor(this, parameters[i], i);
        }
        this.parameterTypes = executable.getParameterTypes();
    }

    /**
     * Returns {@code "constructor"} for a {@link ConstructorDescriptor} or {@code "method"} for a {@link MethodDescriptor}.
     *
     * @return the descriptor type
     */
    public abstract String descriptorTypeName();

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return executable.getAnnotation(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] getAnnotations() {
        return executable.getAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return executable.getAnnotationsByType(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return executable.getDeclaredAnnotation(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return executable.getDeclaredAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return executable.getDeclaredAnnotationsByType(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public final Class<?> getDeclaringClass() {
        return executable.getDeclaringClass();
    }

    /** {@inheritDoc} */
    @Override
    public final int getModifiers() {
        return executable.getModifiers();
    }

    /**
     * Returns an array of parameter mirrors of the executable.
     *
     * @return an array of parameter mirrors of the executable
     */
    // TODO fix
    public final ParameterDescriptor[] getParametersUnsafe() {
        return parameters;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return executable.isAnnotationPresent(annotationClass);
    }

    @Override
    public final boolean isSynthetic() {
        return executable.isSynthetic();
    }

    /**
     * Returns true if the takes a variable number of arguments, otherwise false.
     *
     * @return true if the takes a variable number of arguments, otherwise false.
     * 
     * @see Method#isVarArgs()
     * @see Constructor#isVarArgs()
     */
    public final boolean isVarArgs() {
        return executable.isVarArgs();
    }

    /**
     * Creates a new Executable from this descriptor.
     *
     * @return a new Executable from this descriptor
     */
    public abstract Executable newExecutable();

    /**
     * Returns the number of formal parameters (whether explicitly declared or implicitly declared or neither) for the
     * underlying executable.
     *
     * @return The number of formal parameters for the method this object represents
     *
     * @see Executable#getParameterCount()
     * @see Method#getParameterCount()
     * @see Constructor#getParameterCount()
     */
    public final int parameterCount() {
        return parameters.length;
    }

    /**
     * Unreflects this executable.
     * 
     * @param lookup
     *            the lookup object to use for unreflecting this executable
     * @return a MethodHandle corresponding to this executable
     * @throws IllegalAccessException
     *             if the lookup object does not have access to the executable
     * @see Lookup#unreflect(Method)
     * @see Lookup#unreflectConstructor(Constructor)
     */
    public abstract MethodHandle unreflect(MethodHandles.Lookup lookup) throws IllegalAccessException;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ExecutableDescriptor of(Executable executable) {
        requireNonNull(executable, "executable is null");
        return executable instanceof Constructor ? ConstructorDescriptor.of((Constructor) executable) : MethodDescriptor.of(executable);
    }
}

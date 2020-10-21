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
package packed.internal.introspection;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import app.packed.introspection.ConstructorDescriptor;
import app.packed.introspection.ExecutableDescriptor;
import app.packed.introspection.MethodDescriptor;
import app.packed.introspection.ParameterDescriptor;
import app.packed.introspection.VariableDescriptor;

/**
 * A parameter descriptor.
 * <p>
 * Unlike the {@link Parameter} class, this interface contains no mutable operations, so it can be freely shared.
 * 
 * @apiNote In the future, if the Java language permits, {@link ParameterDescriptor} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
public final class PackedParameterDescriptor implements VariableDescriptor, ParameterDescriptor {

    /** The executable that declares the parameter. */
    private final PackedExecutableDescriptor declaringExecutable;

    /** The index of the parameter. */
    private final int index;

    /** The actual parameter this instance is an descriptor for. */
    private final Parameter parameter;

    /**
     * Creates a new descriptor
     *
     * @param declaringExecutable
     *            the executable that declares the parameter
     * @param parameter
     *            the parameter
     * @param index
     *            the index of the parameter
     */
    PackedParameterDescriptor(PackedExecutableDescriptor declaringExecutable, Parameter parameter, int index) {
        this.declaringExecutable = declaringExecutable;
        this.parameter = parameter;
        this.index = index;
    }

    /** {@inheritDoc} */
    @Override
    public String descriptorTypeName() {
        return "parameter";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof PackedParameterDescriptor) {
            return ((PackedParameterDescriptor) obj).parameter.equals(parameter);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return parameter.getAnnotation(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] getAnnotations() {
        return parameter.getAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return parameter.getAnnotationsByType(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return parameter.getDeclaredAnnotation(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return parameter.getDeclaredAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return parameter.getDeclaredAnnotationsByType(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getDeclaringClass() {
        return parameter.getDeclaringExecutable().getDeclaringClass();
    }

    /** {@inheritDoc} */
    @Override
    public ExecutableDescriptor getDeclaringExecutable() {
        return ExecutableDescriptor.from(parameter.getDeclaringExecutable());
    }

    /** {@inheritDoc} */
    @Override
    public int getModifiers() {
        return parameter.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return parameter.getName();
    }

    /** {@inheritDoc} */
    @Override
    public Type getParameterizedType() {
        Class<?> dc = getDeclaringClass();
        // Workaround for https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8213278
        if (index() > 0 && (dc.isLocalClass() || (dc.isMemberClass() && !Modifier.isStatic(dc.getModifiers())))) {
            return declaringExecutable.executable.getGenericParameterTypes()[index() - 1];
        } else {
            return parameter.getParameterizedType();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return parameter.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public int index() {
        return index;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return parameter.isAnnotationPresent(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNamePresent() {
        return parameter.isNamePresent();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isVarArgs() {
        return parameter.isVarArgs();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return parameter.toString();
    }

    /**
     * Creates a new parameter descriptor from the specified parameter.
     *
     * @param parameter
     *            the parameter to create a descriptor for
     * @return a new parameter descriptor
     */
    public static ParameterDescriptor from(Parameter parameter) {
        requireNonNull(parameter, "parameter is null");

        PackedExecutableDescriptor em;
        if (parameter.getDeclaringExecutable() instanceof Constructor) {
            em = (PackedExecutableDescriptor) ConstructorDescriptor.from((Constructor<?>) parameter.getDeclaringExecutable());
        } else {
            em = (PackedExecutableDescriptor) MethodDescriptor.from((Method) parameter.getDeclaringExecutable());
        }

        // parameter.index is not visible, so we need to iterate through all parameters to find the right one
        if (em.parameterCount() == 1) {
            return em.getParametersUnsafe()[0];
        } else {
            for (PackedParameterDescriptor p : em.getParametersUnsafe()) {
                if (p.parameter.equals(parameter)) {
                    return p;
                }
            }
        }
        throw new IllegalStateException("Could not find parameter " + parameter);// We should never get to here
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> name() {
        return Optional.of(parameter.getName());
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> rawType() {
        return parameter.getType();
    }

    /** {@inheritDoc} */
    @Override
    public TypeToken<?> type() {
        return TypeToken.fromParameter(parameter);
    }
}
//
/// **
// * Creates a new {@link Parameter} corresponding to this descriptor.
// * <p>
// * This method always creates a new parameter to avoid giving access to the underlying mutable {@link Executable
// * Parameter#getDeclaringExecutable()}.
// *
// * @return a new parameter
// */
// Parameter newParameter() {
// // Parameter is immutable, but it contains a reference to its declaring executable exposed via
// // Parameter#getDeclaringExecutable. So we need to create a new copy
// return declaringExecutable.newExecutable().getParameters()[index];
// }
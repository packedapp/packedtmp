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
package packed.internal.base.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

import app.packed.base.reflect.ExecutableDescriptor;
import app.packed.base.reflect.ParameterDescriptor;

/** The default implementation of {@link ExecutableDescriptor}. */
public abstract class PackedExecutableDescriptor implements ExecutableDescriptor {

    /** The executable */
    final Executable executable;

    /** An array of the parameter descriptor for this executable */
    private final PackedParameterDescriptor[] parameters;

    /** The parameter types of the executable. */
    final Class<?>[] parameterTypes;

    /**
     * Creates a new descriptor from the specified executable.
     *
     * @param executable
     *            the executable to mirror
     */
    PackedExecutableDescriptor(Executable executable) {
        this.executable = executable;
        // Create these lazily...
        Parameter[] parameters = executable.getParameters();
        this.parameters = new PackedParameterDescriptor[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            this.parameters[i] = new PackedParameterDescriptor(this, parameters[i], i);
        }
        this.parameterTypes = executable.getParameterTypes();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?>[] getParameterTypes() {
        return executable.getParameterTypes();
    }

    /** {@inheritDoc} */

    @Override
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

    /** {@inheritDoc} */
    // TODO fix
    @Override
    public final PackedParameterDescriptor[] getParametersUnsafe() {
        return parameters;
    }

    /** {@inheritDoc} */
    @Override
    public final ParameterDescriptor getParameter(int index) {
        // TODO range check
        return parameters[index];
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return executable.isAnnotationPresent(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isSynthetic() {
        return executable.isSynthetic();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isVarArgs() {
        return executable.isVarArgs();
    }

    /** {@inheritDoc} */
    @Override
    public final int parameterCount() {
        return parameters.length;
    }

    /** {@inheritDoc} */
    // If we have a non method based version....
    // We can use Lookup.find(xxxxx)
    @Override
    public abstract MethodHandle unreflect(MethodHandles.Lookup lookup) throws IllegalAccessException;

}

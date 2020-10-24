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
package packed.internal.inject.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Optional;

import app.packed.base.AnnotatedVariable;
import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import packed.internal.util.ReflectionUtil;

/**
 * A parameter descriptor.
 * <p>
 * Unlike the {@link Parameter} class, this interface contains no mutable operations, so it can be freely shared.
 * 
 */
public final class PackedParameterDescriptor implements AnnotatedVariable {

    /** The index of the parameter. */
    private final int index;

    /** The actual parameter this instance is an descriptor for. */
    private final Parameter parameter;

    /**
     * Creates a new descriptor
     *
     * @param parameter
     *            the parameter
     * @param index
     *            the index of the parameter
     */
    PackedParameterDescriptor(Parameter parameter, int index) {
        this.parameter = parameter;
        this.index = index;
    }

    public Executable unsafeExecutable() {
        return parameter.getDeclaringExecutable();
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

    public Class<?> getDeclaringClass() {
        return parameter.getDeclaringExecutable().getDeclaringClass();
    }

    public int getModifiers() {
        return parameter.getModifiers();
    }

    public String getName() {
        return parameter.getName();
    }

    public Type getParameterizedType() {
        Class<?> dc = getDeclaringClass();
        // Workaround for https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8213278
        if (index() > 0 && (dc.isLocalClass() || (dc.isMemberClass() && !Modifier.isStatic(dc.getModifiers())))) {
            return parameter.getDeclaringExecutable().getGenericParameterTypes()[index() - 1];
        } else {
            return parameter.getParameterizedType();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return parameter.hashCode();
    }

    public int index() {
        return index;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return parameter.isAnnotationPresent(annotationClass);
    }

    public boolean isNamePresent() {
        return parameter.isNamePresent();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return parameter.toString();
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
        Type t = ReflectionUtil.getParameterizedType(parameter, index);
        return TypeToken.fromType(t);
    }
}

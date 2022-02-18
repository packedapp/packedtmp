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
package packed.internal.bean.hooks.variable;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Optional;

import app.packed.base.TypeToken;
import app.packed.base.Variable;

/**
 *
 */
public record ParameterVariable(Parameter parameter) implements Variable {

    public Executable getDeclaringExecutable() {
        return parameter.getDeclaringExecutable();
    }

    public AnnotatedType getAnnotatedType() {
        return parameter.getAnnotatedType();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return parameter.getAnnotation(annotationClass);
    }

    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return parameter.getAnnotationsByType(annotationClass);
    }

    public Annotation[] getDeclaredAnnotations() {
        return parameter.getDeclaredAnnotations();
    }

    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return parameter.getDeclaredAnnotation(annotationClass);
    }

    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return parameter.getDeclaredAnnotationsByType(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return parameter.getAnnotations();
    }

    /**
     * @param e
     */
    public ParameterVariable {
        requireNonNull(parameter);
    }

    public Optional<String> name() {
        return Optional.of(parameter.getName());
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getType() {
        return parameter.getType();
    }

    /** {@inheritDoc} */
    @Override
    public TypeToken<?> typeToken() {
        throw new UnsupportedOperationException();
    }
}

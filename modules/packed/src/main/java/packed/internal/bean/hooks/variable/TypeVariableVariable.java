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
import java.lang.reflect.TypeVariable;
import java.util.Optional;

import app.packed.base.TypeToken;
import app.packed.base.Variable;

/**
 *
 */
public record TypeVariableVariable(TypeVariable<?> typeVariable) implements Variable {

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return typeVariable.isAnnotationPresent(annotationClass);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return typeVariable.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return typeVariable.getAnnotations();
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return typeVariable.getAnnotationsByType(annotationClass);
    }

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return typeVariable.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return typeVariable.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return typeVariable.getDeclaredAnnotations();
    }

    /**
     * @param e
     */
    public TypeVariableVariable {
        requireNonNull(typeVariable);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getType() {
        return typeVariable.getGenericDeclaration().getClass();
    }

    public Optional<String> name() {
        return Optional.of(typeVariable.getName());
    }

    /** {@inheritDoc} */
    @Override
    public TypeToken<?> typeToken() {
        throw new UnsupportedOperationException();
    }
}

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
package app.packed.binding;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Implementation of {@link Variable}. We basically wrap an annotation part and a type part. */
record PackedVariable(AnnotatedElement annotatedElement, PackedVariableType type) implements Variable {

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Annotation[] annotations = annotatedElement.getAnnotations();
        sb.append(Stream.of(annotations).map(Annotation::toString).collect(Collectors.joining(" ")));
        if (annotations.length > 0) {
            sb.append(" ");
        }
        sb.append(type.toString());
        return sb.toString();
    }

    public boolean equals(Object obj) {
        // We treat the ordering of annotations as significant
        return obj instanceof Variable v && getType().equals(v.getType()) && Arrays.equals(getAnnotations(), v.getAnnotations());
    }

    public int hashCode() {
        return type.hashCode() ^ Arrays.hashCode(annotatedElement.getAnnotations());
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return annotatedElement.isAnnotationPresent(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return annotatedElement.getAnnotationsByType(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return annotatedElement.getDeclaredAnnotation(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return annotatedElement.getDeclaredAnnotationsByType(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return annotatedElement.getAnnotation(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] getAnnotations() {
        return annotatedElement.getAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return annotatedElement.getDeclaredAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getRawType() {
        return type.rawType();
    }

    /** {@inheritDoc} */
    @Override
    public Type getType() {
        return type.type();
    }
}

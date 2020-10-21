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
package packed.internal.component.variable;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import app.packed.base.AnnotatedVariable;

/**
 *
 */
public abstract class AbstractVariable implements AnnotatedVariable {

    final AnnotatedElement e;

    AbstractVariable(AnnotatedElement e) {
        this.e = requireNonNull(e);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return e.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return e.getAnnotations();
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return e.getAnnotationsByType(annotationClass);
    }

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return e.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return e.getDeclaredAnnotations();
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return e.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return e.isAnnotationPresent(annotationClass);
    }
}

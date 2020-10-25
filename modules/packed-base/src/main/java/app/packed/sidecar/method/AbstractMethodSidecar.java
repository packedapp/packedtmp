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
package app.packed.sidecar.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.TypeVariable;

/**
 *
 */
abstract class AbstractMethodSidecar implements GenericDeclaration, Member {

    protected abstract void configure();

    @Override
    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    @Override
    public final Annotation[] getAnnotations() {
        return null;
    }

    @Override
    public final <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return GenericDeclaration.super.getAnnotationsByType(annotationClass);
    }

    @Override
    public final <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return GenericDeclaration.super.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public final Annotation[] getDeclaredAnnotations() {
        return null;
    }

    @Override
    public final <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return GenericDeclaration.super.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public final Class<?> getDeclaringClass() {
        return null;
    }

    @Override
    public final int getModifiers() {
        return 0;
    }

    @Override
    public final String getName() {
        return null;
    }

    @Override
    public final TypeVariable<?>[] getTypeParameters() {
        return null;
    }

    @Override
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return GenericDeclaration.super.isAnnotationPresent(annotationClass);
    }

    @Override
    public final boolean isSynthetic() {
        return false;
    }
}

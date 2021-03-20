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
package app.packed.base;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Consumer;

/**
 *
 */
// maaske extends AnnotatedElement og
public interface MetaAnnotatedElement {

    /**
     * Returns this element's annotation for the specified type if such an annotation is <em>present</em>, else null.
     *
     * @param <T>
     *            the type of the annotation to query for and return if present
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return this element's annotation for the specified annotation type if present on this element, else null
     * @throws NullPointerException
     *             if the given annotation class is null
     * @since 1.5
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * Returns annotations that are <em>present</em> on this element.
     *
     * If there are no annotations <em>present</em> on this element, the return value is an array of length 0.
     *
     * The caller of this method is free to modify the returned array; it will have no effect on the arrays returned to
     * other callers.
     *
     * @return annotations present on this element
     * @since 1.5
     */
    Annotation[] getAnnotations();

    /**
     * Returns annotations that are <em>associated</em> with this element.
     *
     * If there are no annotations <em>associated</em> with this element, the return value is an array of length 0.
     *
     * The difference between this method and {@link #getAnnotation(Class)} is that this method detects if its argument is a
     * <em>repeatable annotation type</em> (JLS {@jls 9.6}), and if so, attempts to find one or more annotations of that
     * type by "looking through" a container annotation.
     *
     * The caller of this method is free to modify the returned array; it will have no effect on the arrays returned to
     * other callers.
     *
     * @implSpec The default implementation first calls passing {@code
     * annotationClass} as the argument. If the returned array has length greater than zero, the array is returned. If the
     *           returned array is zero-length and this {@code AnnotatedElement} is a class and the argument type is an
     *           inheritable annotation type, and the superclass of this {@code AnnotatedElement} is non-null, then the
     *           returned result is the result of calling {@link #getAnnotationsByType(Class)} on the superclass with {@code
     * annotationClass} as the argument. Otherwise, a zero-length array is returned.
     *
     * @param <T>
     *            the type of the annotation to query for and return if present
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return all this element's annotations for the specified annotation type if associated with this element, else an
     *         array of length zero
     * @throws NullPointerException
     *             if the given annotation class is null
     * @since 1.8
     */
    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass);

    /**
     * Returns true if an annotation for the specified type is <em>present</em> on this element, else false. This method is
     * designed primarily for convenient access to marker annotations.
     *
     * <p>
     * The truth value returned by this method is equivalent to: {@code getAnnotation(annotationClass) != null}
     *
     * @implSpec The default implementation returns {@code
     * getAnnotation(annotationClass) != null}.
     *
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return true if an annotation for the specified annotation type is present on this element, else false
     * @throws NullPointerException
     *             if the given annotation class is null
     * @since 1.5
     */
    default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    default boolean hasMetaAnnotations() {
        return true;
    }

    // Vi cacher ikke
    
    // Enhver annotation maa have 
    
    /**
     * <p>
     * This class does not cache anything. If you need to cache stuff you have to do it yourself
     * 
     * @param lookup
     * @param element
     * @return a meta annotated element
     */
    static MetaAnnotatedElement of(MethodHandles.Lookup lookup, AnnotatedElement element) {
        throw new UnsupportedOperationException();
    }
    
    static MetaAnnotatedElement forEach(MethodHandles.Lookup lookup, AnnotatedElement element, Consumer<? super Annotation> action) {
        throw new UnsupportedOperationException();
    }
}

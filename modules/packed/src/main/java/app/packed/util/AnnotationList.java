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
package app.packed.util;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Consumer;

import internal.app.packed.util.PackedAnnotationList;

/**
 * An annotation reader can be used to process annotations on bean elements.
 *
 * @see AnnotatedElement
 */
public sealed interface AnnotationList extends Iterable<Annotation> permits PackedAnnotationList {

    /**
     * {@return {@code true} if this list contains the specified annotation}
     *
     * @param annotation
     *            annotation whose presence is to be tested
     */
    boolean contains(Annotation annotation);

    boolean containsType(Class<? extends Annotation> annotationClass);

    default <T extends Annotation> void ifPresent(Class<T> annotationClass, Consumer<T> consumer) {
        T t = readRequired(annotationClass);
        consumer.accept(t);
    }

    /** {@return whether or not there are any annotations to read.} */
    boolean isEmpty();

    // Det er taenk
    Annotation[] readAnyOf(Class<?>... annotationTypes);

    /**
     * Returns a annotation of the specified type or throws {@link BeanInstallationException} if the annotation is not
     * present
     *
     * @param <T>
     *            the type of the annotation to query for and return if present
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return the annotation for the specified annotation type if present
     *
     * @throws BeanInstallationException
     *             if the specified annotation is not present or the annotation is a repeatable annotation and there are not
     *             exactly 1 occurrences of it
     *
     * @see AnnotatedElement#getAnnotation(Class)
     */
    //// foo bean was expected method to dddoooo to be annotated with
    <T extends Annotation> T readRequired(Class<T> annotationClass);

    /** {@return the number of annotations in the list.} */
    int size();

    Annotation[] toArray();

    List<Annotation> toList();

    /** {@return an empty annotation list.} */
    static AnnotationList of() {
        return PackedAnnotationList.EMPTY;
    }

    static AnnotationList of(Annotation annotation) {
        requireNonNull(annotation, "annotation is null");
        return new PackedAnnotationList(new Annotation[] { annotation });
    }

    static AnnotationList of(Annotation... annotations) {
        Annotation[] a = annotations.clone();
        // check for null
        return new PackedAnnotationList(a);
    }

    static AnnotationList of(Annotation annotation1, Annotation annotation2) {
        requireNonNull(annotation1, "annotation1 is null");
        requireNonNull(annotation2, "annotation2 is null");
        return new PackedAnnotationList(new Annotation[] { annotation1, annotation2 });
    }
}
// Q) Skal vi bruge den udefra beans???
// A) Nej vil ikke mene vi beskaeftiger os med andre ting hvor vi laeser det.
// Altsaa hvad med @Composite??? Det er jo ikke en bean, det bliver noedt til at vaere fake metoder...
// Paa hver bean som bruger den...
// Vi exponere den jo ikke, saa kan jo ogsaa bare bruge den...

// I think the only we reason we call it BeanAnnotationReader is because
// if we called AnnotationReader is should really be located in a utility package

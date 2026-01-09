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
package internal.app.packed.util;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.util.AnnotationList;

/** Implementation of {@link AnnotationList}. */
public record PackedAnnotationList(Annotation... annotations) implements AnnotationList {

    public PackedAnnotationList(Annotation a) {
        this(new Annotation[] { a });
    }

    public static final PackedAnnotationList EMPTY = new PackedAnnotationList(new Annotation[0]);

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof PackedAnnotationList other && Arrays.equals(annotations, other.annotations);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Arrays.hashCode(annotations);
    }

    /**
     * @param annotations
     * @return
     */
    public static PackedAnnotationList of(Annotation... annotations) {
        return new PackedAnnotationList(annotations);
    }

    public static PackedAnnotationList ofUnsafe(Annotation... annotations) {
        // TODO check for null
        return new PackedAnnotationList(annotations.clone());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(Class<? extends Annotation> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPresent(Annotation annotation) {
        requireNonNull(annotation, "annotation is null");
        for (Annotation annotation2 : annotations) {
            if (annotation2.equals(annotation)) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] readAnyOf(Class<?>... annotationTypes) {
        return null;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> Optional<T> read(Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass) {
                return Optional.of((T) annotation);
            }
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> T readRequired(Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass) {
                return (T) annotation;
            }
        }
        throw new IllegalStateException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return annotations.length == 0;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return annotations.length;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Annotation> iterator() {
        return List.of(annotations).iterator();
    }

    @Override
    public void forEach(Consumer<? super Annotation> action) {
        requireNonNull(action, "action is null");
        for (Annotation annotation : annotations) {
            action.accept(annotation);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] toArray() {
        return annotations.clone();
    }

    /** {@inheritDoc} */
    @Override
    public List<Annotation> toList() {
        return List.of(annotations);
    }

    @Override
    public String toString() {
        return Arrays.toString(annotations);
    }
}

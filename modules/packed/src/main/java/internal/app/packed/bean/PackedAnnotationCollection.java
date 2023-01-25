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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import app.packed.bean.BeanIntrospector.AnnotationCollection;

/** Implementation of {@link AnnotationCollection}. */
public record PackedAnnotationCollection(Annotation... annotations) implements AnnotationCollection {

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == annotationClass) {
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
    public <T extends Annotation> T readRequired(Class<T> annotationClass) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == annotationClass) {
                return (T) annotations[i];
            }
        }
        throw new IllegalStateException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return annotations.length != 0;
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
        for (int i = 0; i < annotations.length; i++) {
            action.accept(annotations[i]);
        }
    }
}

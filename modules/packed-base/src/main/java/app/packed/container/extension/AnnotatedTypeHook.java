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
package app.packed.container.extension;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import packed.internal.container.model.ComponentModel;

/** A hook representing a instance whose type is annotated with a specific annotation type. */
// Kan f.eks. bruges til @Provide, hvor vi i saa fald skal have adgang til en instans.......
// Hvilket foerst kan ske paa runtime.... Ligesom AnnotatedMethods + Annotated Fields...
public final class AnnotatedTypeHook<T extends Annotation> {

    /** The annotation value. */
    private final T annotation;

    /** The builder for the component type. */
    final ComponentModel.Builder builder;

    /** The annotated type. */
    private final Class<?> type;

    /**
     * Creates a new hook instance.
     * 
     * @param builder
     *            the builder for the component type
     * @param type
     *            the annotated type
     * @param annotation
     *            the annotation value
     */
    AnnotatedTypeHook(ComponentModel.Builder builder, Class<?> type, T annotation) {
        this.builder = requireNonNull(builder);
        this.type = requireNonNull(type);
        this.annotation = requireNonNull(annotation);
    }

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    public T annotation() {
        return annotation;
    }

    /**
     * Returns the annotated type.
     * 
     * @return the annotated type
     */
    public Class<?> type() {
        return type;
    }
}

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
package app.packed.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import packed.internal.hook.HookProcessor;

/** A hook representing a instance whose type is annotated with a specific annotation type. */
// Kan f.eks. bruges til @Provide, hvor vi i saa fald skal have adgang til en instans.......
// Hvilket foerst kan ske paa runtime.... Ligesom AnnotatedMethods + Annotated Fields...
public final class AnnotatedTypeHook<T extends Annotation> implements Hook {

    /** The annotation value. */
    private final T annotation;

    /** The builder for the component type. */
    final HookProcessor controller;

    /** The annotated type. */
    private final Class<?> type;

    /**
     * Creates a new hook instance.
     * 
     * @param controller
     *            the builder for the component type
     * @param type
     *            the annotated type
     * @param annotation
     *            the annotation value
     */
    AnnotatedTypeHook(HookProcessor controller, Class<?> type, T annotation) {
        this.controller = requireNonNull(controller);
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

    public HookApplicator<Object> applicator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the type that was annotated.
     * 
     * @return the type that was annotated
     */
    public Class<?> type() {
        return type;
    }
}

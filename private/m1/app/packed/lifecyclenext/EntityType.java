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
package app.packed.lifecyclenext;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import app.packed.container.Extension;
import app.packed.container.ExtensionSidecar;
import app.packed.container.MemberOfExtension;

/**
 *
 */

// ComponentType
// ElementType

// Ideen er vi har alle de entitites typer inde, som brugere kan definere

// Hvis de skal instantieres af Packed
// Hvis de har metoder der skal invokeres af Packed
// Hvis de har Class annotations der skal lases

// Maybe have a list (static methods) of all supported framework class annotations
// These are always checked. And Packed will fail if they are out of place...

// De kan sagtens vaere en bit istedet naar vi skal teste... 

// Supports Custom states... <- Only think component does... boolean supportsCustomStates(return this == COMPONENT)
public enum EntityType {

    // Maybe we can encode most of this as a long...
    // Bundle(INJECT_BY_USER | CLASS_ANNO_FOOANNOTATION)

    BUNDLE(Set.of()),

    EXTENSION(Set.of(ExtensionSidecar.class)),

    WIRELET_PIPELINE(Set.of(MemberOfExtension.class));

    // Activity..
    // States, Available Contexts

    private final Set<Class<? extends Annotation>> classAnnotations;

    private EntityType(Set<Class<? extends Annotation>> classAnnotations) {
        this.classAnnotations = requireNonNull(classAnnotations);
    }

    /**
     * Returns a set of class annotations that Packed will look for when processing the entity.
     * 
     * @return a set of class annotations that Packed will look for when processing the entity
     */
    public Set<Class<? extends Annotation>> classAnnotations() {
        return classAnnotations;
    }

    public boolean instantiatedByRuntime() {
        // Components can be both...
        // Bundle is always user instantiated...
        return false;
    }

    public boolean instantiatedByUser() {
        // Der er vel 3 modeller
        // Always Packed, Always User, Mixed..
        return false;
    }

    /**
     * Returns any super class the entity must inherit..
     * 
     * @return any super class the entity must inherit.
     */
    public Optional<Class<?>> superClass() {
        return Optional.of(Extension.class);
    }

    public boolean hasLifecycle() {
        // Er der nogen der ikke har. Andet end sikker wirelets...
        return true;
    }

    @Override
    public String toString() {
        String st = classAnnotations.stream().map(e -> e.getCanonicalName()).collect(Collectors.joining(", ", "[", "]"));
        return super.toString() + " {classAnnotations = " + st + "}";
    }

    public static void main(String[] args) {
        System.out.println(WIRELET_PIPELINE);
        System.out.println(EXTENSION);
    }
}

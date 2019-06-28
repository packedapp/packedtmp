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

import java.lang.annotation.Annotation;
import java.util.Collection;

/** A hook representing a instance whose type is annotated with a specific type. */
public interface AnnotatedTypeHook<T extends Annotation> extends Hook {

    /**
     * Returns the actual type that is annotated.
     * 
     * @return the actual type that is annotated
     */
    Class<?> actualType(); // What if AOP?

    /**
     * Returns a collection of fields annotated with the specified hook annotation. The hooks are returned in any order.
     * 
     * @param <S>
     *            the type of stuff
     * @param annotationType
     *            the type of annotation
     * @return stuff
     */
    /// nahhhh....Hvad hvis de ikke er eksporteret... Og vi har vel svaert ved at filtrer dem....
    <S extends Annotation> Collection<AnnotatedFieldHook<S>> annotatedFieldHooks(Class<S> annotationType);

    <S extends Annotation> Collection<OldAnnotatedMethodHook<S>> annotatedMethodHooks(Class<S> annotationType);

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    T annotation();

    /**
     * Returns the instance.
     * 
     * @return the instance
     * @throws IllegalStateException
     *             if not yet instantiated
     */
    // component.instance();
    Object instance();
}
// TODO skal vi ogsaa have dem paa InstanceOfHook??? Ja det syntes jeg

// Eneste problem er hvis vi smider exceptions...
// <S extends Annotation> void forEachAnnotatedFieldHook(Class<S> annotationType, Consumer<? super
// AnnotatedFieldHook<S>> consumer);

// <S extends Annotation> void forEachAnnotatedMethodHook(Class<S> annotationType, Consumer<? super
// AnnotatedMethodHook<S>> consumer);

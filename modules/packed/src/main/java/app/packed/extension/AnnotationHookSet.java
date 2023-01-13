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
package app.packed.extension;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

/**
 *
 */

// Bare brug BeanInstrospector.AnnotationReader?
// Altsaa begge 2 er vel en collection af annotations.
// Hvor vi gerne vil extract og teste

// Problemet er lidt vi maaske gerne vil checke at der ikke er nogen der misbruger hooks

// @Inject + @FakeInject(Andet module, peger paa BaseExtension)

// Det maaske fint nok? at vi ignorer FakeInject hvis Inject er der
// Alternativt er vi skal til at consume alle annotations
// Og lave den immutablex



// Det er vel ikke et set fordi vi kan have det samme element 2 gange
// AnnotationCollection
public interface AnnotationHookSet {

    <A extends Annotation> void ifPresent(Class<A> annotationType, Consumer<? super A> action);
}

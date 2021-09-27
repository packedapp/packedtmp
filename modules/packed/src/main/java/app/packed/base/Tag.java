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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;

/** A {@link Qualifier} that holds a generic string. */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
// vil maaske gerne omnavngive den... Saa folk ikke tror man navngiver componenter...
// Classifier??? Labelled

// Maybe just Tag, it is b
public @interface Tag {

    /** An annotation maker that can create {@link Tag} instances. */
    static final AnnotationMaker<Tag> MAKER = AnnotationMaker.of(MethodHandles.lookup(), Tag.class);

    /**
     * Returns the tag.
     * 
     * @return the tag
     */
    // Problemet er lidt at vi helst vil vaere ligeglade med raekkefolgen...
    // Det er bare ikke muligt... Og det er maaske okay...
    // Og vi boer faktisk ogsaa acceptere dublikater... Hmmm

    String value();
}

/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A {@link Qualifier} that holds a generic string. */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Target({ ElementType.TYPE_USE, ElementType.PARAMETER })
// vil maaske gerne omnavngive den... Saa folk ikke tror man navngiver componenter...
// Classifier??? Labelled

// Tror de
// Maybe just Tag, it is b

// Component Tags??? Then I would rather have @Bean(componentTags="asdasd")
// Was KeyClassfier
public @interface StringQualifier {

    /**
     * Returns the tag.
     *
     * @return the tag
     */
    // Problemet er lidt at vi helst vil vaere ligeglade med raekkefolgen...
    // Det er bare ikke muligt... Og det er maaske okay...
    // Og vi boer faktisk ogsaa acceptere dublikater... Hmmm
    // Hvis vi har et array er vi ikke ligeglade med raekkefolgen af values

    String value();
}

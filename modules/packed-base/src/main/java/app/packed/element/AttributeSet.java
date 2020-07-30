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
package app.packed.element;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

/**
 *
 */

//AttributeMap???

// Kopiere noget mere fra Optional...

// Syntes navngivningen skal gaa igen i Config...

//attributes.ifPresent(ServiceExtension.CONTAINS, e->sysout("number of foo " + e);
public interface AttributeSet {

    /**
     * Returns a set of all the attributes that are present in this set.
     * 
     * @return a set of all the attributes that are present in this set
     */
    Set<Attribute<?>> attributes();// maybe just keys.... But then we must rename AttributeSet->AttributeMap

    Set<Map.Entry<Attribute<?>, Object>> entrySet();

    /**
     * If the specified attribute is present, returns the value of the attribute, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the non-{@code null} value of the specified attribute
     * @throws NoSuchElementException
     *             if the specified attribute is not present
     */
    <T> T get(Attribute<T> attribute);

    /**
     * If an attribute is present, performs the given action with the value of the attribute, otherwise does nothing.
     *
     * @param action
     *            the action to be performed, if an attribute is present
     */
    <T> void ifPresent(Attribute<T> attribute, Consumer<T> action);

    <T> T orElse(Attribute<T> attribute, T other);
}

// AttributeSet open(Lookup lookup);
// Will show stuff that is hidden to....

// or privateLookup idk
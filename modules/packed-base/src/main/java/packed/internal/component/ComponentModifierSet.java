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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

/**
 * An immutable set of component modifiers.
 */
public interface ComponentModifierSet extends Iterable<ComponentModifier> {

    /**
     * Returns whether or not this contains the specified modifier
     * 
     * @param modifier
     *            the modifier to test
     * @return true if this set contains the specified modifier, otherwise false
     */
    boolean contains(ComponentModifier modifier);


    /**
     * Returns a new array containing all of the modifiers in this set.
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this set. The caller is thus free to
     * modify the returned array.
     *
     * @return an array containing all the modifiers in this set
     */
    // toModifierArray? if we want to implement Set
    ComponentModifier[] toArray();

    /**
     * Returns an set containing the specified modifiers.
     * 
     * @param modifiers the modifiers to include in the set
     * 
     * @return an set containing the single specified modifier
     *
     * @see ComponentModifier#toSet()
     * @see ComponentModifier#toSet()
     */
    static ComponentModifierSet of(ComponentModifier... modifiers) {
        requireNonNull(modifiers, "modifiers is null");
        int b = 0;
        for (int i = 0; i < modifiers.length; i++) {
            b |= modifiers[i].bits();
        }
        return new PackedComponentModifierSet(b);
    }
}

// @apiNote this interface does not have an isEmpty method as we have yet to find any components without modifiers
// Altsaa 
//boolean isEmpty();

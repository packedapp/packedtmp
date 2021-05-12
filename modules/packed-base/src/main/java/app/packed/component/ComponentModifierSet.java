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
package app.packed.component;

import static java.util.Objects.requireNonNull;

import packed.internal.component.PackedComponentModifierSet;

/**
 * An immutable set of component modifiers.
 */
// Skal vi ikke bare extende AbstractSet??? Nah.. saa meget fluff.. maybe just a toSet??
public interface ComponentModifierSet extends Iterable<ComponentModifier> {

    /**
     * Returns whether or not this contains the specified modifier
     * 
     * @param modifier
     *            the modifier to test
     * @return true if this set contains the specified modifier, otherwise false
     */
    boolean contains(ComponentModifier modifier);


    // boolean containsAll(Collection<ComponentModifier> c);
    /**
     * Returns whether or not this set contains the {@link ComponentModifier#RUNTIME} modifier.
     * 
     * @return true if this set contains the guest modifier, otherwise false
     */
    default boolean hasRuntime() {
        return contains(ComponentModifier.RUNTIME);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#RUNTIME} modifier.
     * 
     * @return true if this set contains the guest modifier, otherwise false
     */
    default boolean isSingleton() {
        return contains(ComponentModifier.CONSTANT);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#SOURCED} modifier.
     * 
     * @return true if this set contains the source modifier, otherwise false
     */
    default boolean isSource() {
        return contains(ComponentModifier.SOURCED);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#CONTAINER} modifier.
     * 
     * @return true if this set contains the container modifier, otherwise false
     */
    default boolean isStaticClassSource() {
        return contains(ComponentModifier.STATEFUL);
    }

    /**
     * Returns the number of modifiers in this set.
     * 
     * @return the number of modifiers in this set
     */
    int size();

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
     * Returns an empty component modifier set.
     * 
     * @return an empty component modifier set
     */
    static ComponentModifierSet of() {
        return PackedComponentModifierSet.EMPTY;
    }

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
        if (modifiers.length == 0) {
            return of();
        }
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

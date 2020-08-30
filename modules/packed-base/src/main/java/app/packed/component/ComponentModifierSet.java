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

import packed.internal.component.PackedComponentModifierSet;

/**
 * An immutable set of component modifiers.
 */
public interface ComponentModifierSet extends Iterable<ComponentModifier> {

    boolean contains(ComponentModifier modifier);

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#CONTAINER} modifier.
     * 
     * @return true if this set contains the container modifier, otherwise false
     */
    default boolean isContainer() {
        return contains(ComponentModifier.CONTAINER);
    }

    boolean isEmpty();

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#EXTENSION} modifier.
     * 
     * @return true if this set contains the extension modifier, otherwise false
     */
    default boolean isExtension() {
        return contains(ComponentModifier.EXTENSION);
    }

    // boolean containsAll(Collection<ComponentModifier> c);
    /**
     * Returns whether or not this set contains the {@link ComponentModifier#GUEST} modifier.
     * 
     * @return true if this set contains the guest modifier, otherwise false
     */
    default boolean isGuest() {
        return contains(ComponentModifier.GUEST);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#IMAGE} modifier.
     * 
     * @return true if this set contains the image modifier, otherwise false
     */
    default boolean isImage() {
        return contains(ComponentModifier.IMAGE);
    }

    int size();

    /**
     * Returns an array containing all of the modifiers in this set.
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this set. The caller is thus free to
     * modify the returned array.
     *
     * @return an array containing all the modifiers in this set
     */
    ComponentModifier[] toArray();

    ComponentModifierSet with(boolean conditional, ComponentModifier modifier);

    ComponentModifierSet with(ComponentModifier modifier);

    ComponentModifierSet without(boolean conditional, ComponentModifier modifier);

    ComponentModifierSet without(ComponentModifier modifier);

    /**
     * Returns an empty component modifier set.
     * 
     * @return an empty component modifier set
     */
    static ComponentModifierSet of() {
        return PackedComponentModifierSet.EMPTY;
    }

    static ComponentModifierSet of(ComponentModifier m) {
        return new PackedComponentModifierSet(m.bits());
    }

    static ComponentModifierSet of(ComponentModifier m1, ComponentModifier m2) {
        return new PackedComponentModifierSet(m1.bits() | m2.bits());
    }

    static ComponentModifierSet of(ComponentModifier m1, ComponentModifier m2, ComponentModifier m3) {
        return new PackedComponentModifierSet(m1.bits() | m2.bits() | m3.bits());
    }
}

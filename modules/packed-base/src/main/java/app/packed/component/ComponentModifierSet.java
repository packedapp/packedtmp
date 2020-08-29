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

import java.util.Set;

import packed.internal.component.PackedComponentModifierSet;

/**
 *
 */
public interface ComponentModifierSet extends Set<ComponentModifier> {

    boolean contains(ComponentModifier modifier);

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#CONTAINER} modifier.
     * 
     * @return true if this set contains the container modifier, otherwise false
     */
    default boolean isContainer() {
        return contains(ComponentModifier.CONTAINER);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#IMAGE} modifier.
     * 
     * @return true if this set contains the container modifier, otherwise false
     */
    default boolean isImage() {
        return contains(ComponentModifier.IMAGE);
    }

    // THESE METHODS DO NOT GUARANTEE to return the same int across versions
    // Eneste problem er, lad os nu sige vi lige pludselig faar mere en 32 properties...
    // Hvilket jeg ikke regner med er realistisk, men vi har lige pludselig exposed det
    // i vores API.
    public static int toBits(ComponentModifier p) {
        return 1 << p.ordinal();
    }

    public static int toBits(ComponentModifier p1, ComponentModifier p2) {
        return 1 << p1.ordinal() + 1 << p2.ordinal();
    }

    public static Set<ComponentModifier> fromBits(int bits) {
        throw new UnsupportedOperationException();
    }

    static ComponentModifierSet of() {
        return new PackedComponentModifierSet(0);
    }

    static ComponentModifierSet of(ComponentModifier m) {
        return new PackedComponentModifierSet(toBits(m));
    }
}

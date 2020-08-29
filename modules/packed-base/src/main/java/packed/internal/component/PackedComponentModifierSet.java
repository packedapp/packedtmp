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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;

import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;

/**
 * A (possible empty) set of {@link ComponentModifier component modifiers}.
 */
public final class PackedComponentModifierSet extends AbstractSet<ComponentModifier> implements ComponentModifierSet {

    private final static ComponentModifier[] MODIFIERS = ComponentModifier.values();

    private final int modifiers;

    public PackedComponentModifierSet(int modifiers) {
        this.modifiers = modifiers;
    }

    public static boolean isPropertySet(int modifiers, ComponentModifier property) {
        requireNonNull(property, "property is null");
        return (modifiers & (1 << property.ordinal())) != 0;
    }

    @Override
    public boolean isContainer() {
        return isPropertySet(modifiers, ComponentModifier.CONTAINER);
    }

    public static int setProperty(int modifiers, ComponentModifier property) {
        return modifiers | (1 << property.ordinal());
    }

    public static int unsetProperty(int modifiers, ComponentModifier property) {
        return modifiers & ~(1 << property.ordinal());
    }

    public static int setPropertyConditional(int modifiers, boolean setIt, ComponentModifier property) {
        return setIt ? setProperty(modifiers, property) : modifiers;
    }

    public static int unsetPropertyConditional(int modifiers, boolean setIt, ComponentModifier property) {
        return setIt ? unsetProperty(modifiers, property) : modifiers;
    }

    public static int setProperty(int modifiers, ComponentModifier... props) {
        for (ComponentModifier cp : props) {
            modifiers |= (1 << cp.ordinal());
        }
        return modifiers;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ComponentModifier> iterator() {
        int size = size();
        if (size == 0) {
            List<ComponentModifier> l = List.of();
            return l.iterator();
        }
        // There is probably a smarter way.
        return new Iterator<ComponentModifier>() {
            int n = 0;
            int s = size;

            @Override
            public boolean hasNext() {
                return s-- > 0;
            }

            @Override
            public ComponentModifier next() {
                while ((modifiers & (1 << (n++))) == 0) {}
                return MODIFIERS[n - 1];
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return Integer.bitCount(modifiers);
    }

    /** {@inheritDoc} */
    @Override
    public boolean is(ComponentModifier modifier) {
        return isPropertySet(modifiers, modifier);
    }
}

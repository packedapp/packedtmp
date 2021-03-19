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

import java.util.Iterator;
import java.util.List;

import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;

/** Implementation of {@link ComponentModifierSet}. */
public final class PackedComponentModifierSet implements ComponentModifierSet {

    /** An empty modifier set. */
    public static final PackedComponentModifierSet EMPTY = new PackedComponentModifierSet(0);

    public static final int I_ANALYSIS = intOf(ComponentModifier.ANALYSIS);
    public static final int I_BUILD = intOf(ComponentModifier.BUILD_ROOT);
    public static final int I_CONTAINER = intOf(ComponentModifier.CONTAINEROLD);
    public static final int I_CONTAINERNEW = intOf(ComponentModifier.CONTAINER);
    public static final int I_IMAGE = intOf(ComponentModifier.IMAGE_ROOT);
    public static final int I_SOURCE = intOf(ComponentModifier.SOURCED);

    public static final int I_EXTENSION = intOf(ComponentModifier.EXTENSION);
    public static final int I_SHELL = intOf(ComponentModifier.ARTIFACT);
    public static final int I_SINGLETON = intOf(ComponentModifier.CONSTANT);
    public static final int I_STATEFUL = intOf(ComponentModifier.STATEFUL);
    public static final int I_UNSCOPED = intOf(ComponentModifier.UNSCOPED);

    /** An array containing all modifiers. */
    private final static ComponentModifier[] MODIFIERS = ComponentModifier.values();

    /** The modifiers this set wraps. */
    private final int modifiers;

    public PackedComponentModifierSet(int modifiers) {
        this.modifiers = modifiers;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(ComponentModifier modifier) {
        return isSet(modifiers, modifier);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return modifiers == 0;
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
    public ComponentModifier[] toArray() {
        ComponentModifier[] m = new ComponentModifier[size()];
        int i = 0;
        for (ComponentModifier cm : this) {
            m[i++] = cm;
        }
        return m;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        ComponentModifier[] cms = toArray();
        StringBuilder sb = new StringBuilder().append("[").append(cms[0]);
        for (int i = 1; i < cms.length; i++) {
            sb.append(", ").append(cms[i]);
        }
        return sb.append(']').toString();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet with(ComponentModifier modifier) {
        if (isSet(modifiers, modifier)) {
            return this;
        }
        return new PackedComponentModifierSet(add(modifiers, modifier));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet withIf(boolean conditional, ComponentModifier modifier) {
        if (!conditional || isSet(modifiers, modifier)) {
            return this;
        }
        return new PackedComponentModifierSet(add(modifiers, modifier));

    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet without(ComponentModifier modifier) {
        if (!isSet(modifiers, modifier)) {
            return this;
        }
        return new PackedComponentModifierSet(remove(modifiers, modifier));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet withoutIf(boolean conditional, ComponentModifier modifier) {
        if (!conditional || !isSet(modifiers, modifier)) {
            return this;
        }
        return new PackedComponentModifierSet(remove(modifiers, modifier));
    }

    public static int add(int modifiers, ComponentModifier m) {
        return modifiers | intOf(m);
    }

    public static int add(int modifiers, ComponentModifier... ms) {
        for (ComponentModifier cm : ms) {
            modifiers |= intOf(cm);
        }
        return modifiers;
    }

    public static int addIf(int modifiers, boolean conditional, ComponentModifier m) {
        return conditional ? add(modifiers, m) : modifiers;
    }

    public static int intOf(ComponentModifier m) {
        return (1 << m.ordinal());
    }

    public static int intOf(ComponentModifier... ms) {
        int m = 0;
        for (ComponentModifier cm : ms) {
            m |= intOf(cm);
        }
        return m;
    }

    public static int intOf(ComponentModifier m1, ComponentModifier m2) {
        return (1 << m1.ordinal());
    }

    public static boolean isSet(int modifiers, ComponentModifier m) {
        requireNonNull(m, "modifier is null");
        return (modifiers & intOf(m)) != 0;
    }

    public static int remove(int modifiers, ComponentModifier m) {
        return modifiers & ~intOf(m);
    }

    public static int removeIf(int modifiers, boolean conditional, ComponentModifier m) {
        return conditional ? remove(modifiers, m) : modifiers;
    }
}

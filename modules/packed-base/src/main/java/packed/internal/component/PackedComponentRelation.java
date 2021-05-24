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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirror.Relation;
import app.packed.component.ComponentScope;

/** Implementation of {@link Relation}. */
record ComponentSetupRelation(ComponentSetup from, ComponentSetup to, int distance, @Nullable ComponentSetup lcd) implements Relation {

    /** {@inheritDoc} */
    @Override
    public boolean inSameContainer() {
        return from.container == to.container;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ComponentMirror> iterator() {
        if (distance == 0) {
            return List.of((ComponentMirror) from).iterator();
        } else if (distance == 1) {
            return List.of((ComponentMirror) from, (ComponentMirror) to).iterator();
        } else {
            ComponentMirror[] components = new ComponentMirror[distance];

            int i = 0;
            ComponentSetup pc = from;
            while (pc != lcd) {
                components[i++] = pc.mirror();
            }

            components[i++] = lcd.mirror();

            i = components.length - 1;
            pc = to;
            while (pc != lcd) {
                components[i++] = pc.mirror();
            }
            return List.of(components).iterator();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ComponentMirror> findLowestCommonAncestor() {
        return lcd == null ? Optional.empty() : Optional.of(lcd.mirror());
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirror target() {
        return to.mirror();
    }

    @Override
    public boolean inSameApplication() {
        return from.application == to.application;
    }

    @Override
    public boolean isInSame(ComponentScope scope) {
        return from.isInSame(scope, to);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirror source() {
        return from.mirror();
    }

    static Relation of(ComponentSetup from, ComponentSetup to) {
        int fd = from.depth;
        int td = to.depth;
        if (from.lifetime.pool == to.lifetime.pool) {
            if (fd == td) {
                return new ComponentSetupRelation(from, to, 0, from);
            }

            ComponentSetup f = from;
            ComponentSetup t = to;
            int distance = 0;

            if (fd > td) {
                while (fd > td) {
                    f = f.parent;
                    fd--;
                    distance++;
                }
                if (f == to) {
                    return new ComponentSetupRelation(from, to, distance, to);
                }
            } else {
                while (td > fd) {
                    t = t.parent;
                    td--;
                    distance++;
                }
                if (t == from) {
                    return new ComponentSetupRelation(from, to, distance, from);
                }
            }
            while (f != t) {
                f = f.parent;
                t = t.parent;
                distance += 2;
            }
            return new ComponentSetupRelation(from, to, distance, f);
        }
        throw new UnsupportedOperationException();
    }
}

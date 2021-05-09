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

import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirror.Relation;
import app.packed.component.ComponentScope;

/** Implementation of {@link Relation}. */
// source + target vs from + to
/* primitive */ final class PackedComponentInstanceRelation implements Relation {

    private final int distance;

    private final PackedComponentInstance from;

    private final PackedComponentInstance lcd;

    private final PackedComponentInstance to;

    public PackedComponentInstanceRelation(PackedComponentInstance from, PackedComponentInstance to, int distance, PackedComponentInstance lcd) {
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.lcd = lcd;
    }

    /** {@inheritDoc} */
    @Override
    public int distance() {
        return distance;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirror source() {
        return from;
    }

    /** {@inheritDoc} */
    @Override
    public boolean inSameContainer() {
        throw new UnsupportedOperationException();
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
            PackedComponentInstance pc = from;
            while (pc != lcd) {
                components[i++] = pc;
            }

            components[i++] = lcd;

            i = components.length - 1;
            pc = to;
            while (pc != lcd) {
                components[i++] = pc;
            }
            return List.of(components).iterator();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ComponentMirror> findLowestCommonAncestor() {
        return Optional.ofNullable(lcd);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirror target() {
        return to;
    }

    static Relation relation(PackedComponentInstance from, PackedComponentInstance to) {
        int fd = from.depth();
        int td = to.depth();
        if (from.pool == to.pool) {
            if (fd == td) {
                return new PackedComponentInstanceRelation(from, to, 0, from);
            }

            PackedComponentInstance f = from;
            PackedComponentInstance t = to;
            int distance = 0;

            if (fd > td) {
                while (fd > td) {
                    f = f.parent;
                    fd--;
                    distance++;
                }
                if (f == to) {
                    return new PackedComponentInstanceRelation(from, to, distance, to);
                }
            } else {
                while (td > fd) {
                    t = t.parent;
                    td--;
                    distance++;
                }
                if (t == from) {
                    return new PackedComponentInstanceRelation(from, to, distance, from);
                }
            }
            while (f != t) {
                f = f.parent;
                t = t.parent;
                distance += 2;
            }
            return new PackedComponentInstanceRelation(from, to, distance, f);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean inSameApplication() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInSame(ComponentScope scope) {
        // TODO Auto-generated method stub
        return false;
    }
}
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

import app.packed.component.Component;
import app.packed.component.ComponentRelation;

/**
 *
 */
public class SamePodComponentRelation implements ComponentRelation {

    private final int distance;

    private final PackedComponent from;

    private final PackedComponent lcd;

    private final PackedComponent to;

    public SamePodComponentRelation(PackedComponent from, PackedComponent to, int distance, PackedComponent lcd) {
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
    public Component from() {
        return from;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInSameContainer() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInSameSystem() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isStronglyConnected() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWeaklyConnected() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Component> iterator() {
        if (distance == 0) {
            return List.of((Component) from).iterator();
        } else if (distance == 1) {
            return List.of((Component) from, (Component) to).iterator();
        } else {
            Component[] components = new Component[distance];

            int i = 0;
            PackedComponent pc = from;
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
    public Optional<Component> lowestCommonAncestor() {
        return Optional.ofNullable(lcd);
    }

    /** {@inheritDoc} */
    @Override
    public Component to() {
        return to;
    }
}
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
package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.operation.bindings.DependenciesMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.Mirror;

/**
 *
 */

// Describes dependencies between to beans

// operations er direct dependencies

// Find en graph mellem to beans

/** A mirror that describes a relationship between two different beans. */
// Do we support relationship to itself? I would think it was always an error?
public final class BeanRelationshipMirror implements Mirror {

    /** The from bean of the relationship. */
    private final BeanSetup from;

    /** The to bean of the relationship. */
    private final BeanSetup to;

    /**
     * Creates a new mirror
     * 
     * @param from
     *            the from part of the relationship
     * @param to
     *            the to part of the relationship
     */
    BeanRelationshipMirror(BeanSetup from, BeanSetup to) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
    }

    public DependenciesMirror dependencies() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof BeanRelationshipMirror o && (from == o.from && to == o.to);
    }

    /** {@return a mirror of the from bean of the relationship.} */
    public BeanMirror from() {
        return from.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return to.hashCode() ^ from.hashCode();
    }

    /** {@return whether or not the beans are in the same application.} */
    public boolean isInSameApplication() {
        return from.container.application == to.container.application;
    }

    /** {@return whether or not the beans are in the same container.} */
    public boolean isInSameContainer() {
        return from.container == to.container;
    }

    /** {@return whether or not the beans are in the same lifetime.} */
    public boolean isInSameLifetime() {
        return from.lifetime == to.lifetime;
    }

    /** {@return the reverse relationship.} */
    public BeanRelationshipMirror reverse() {
        return new BeanRelationshipMirror(to, from);
    }

    /** {@return a mirror of the to bean of the relationship.} */
    public BeanMirror to() {
        return to.mirror();
    }
}

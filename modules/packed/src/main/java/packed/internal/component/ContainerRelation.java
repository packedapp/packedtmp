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

import java.util.Optional;

import app.packed.application.ComponentMirror;

/**
 * A (component mirror) relation is an unchangeable representation of a directional relationship between two components.
 * <p>
 * It is typically created via {@link ComponentMirror#relationTo(ComponentMirror)}.
 */
// Tror vi skal have en ContainerMirror.Relation og en BeanMirror.Relation
// Det er jo ikke noget der skal vaere i 1. version
// Tror maaske isaer lifetime delen vil vaere interessant
public /* sealed */ interface ContainerRelation extends Iterable<ComponentMirror> {

    /**
     * -1 if {@link #source()} and {@link #target()} are not in the same system. 0 if source and target are identical.
     * 
     * @return the distance between the two components
     */
    int distance();

    /**
     * Finds the lowest common ancestor for the two components if they are in the same system. Or {@link Optional#empty()}
     * if not in the same system.
     * 
     * @return lowest common ancestor for the two components. Or empty if not in the same system
     */
    Optional<ComponentMirror> findLowestCommonAncestor();

    /**
     * Returns whether or not the two components are in the same container.
     * 
     * @return whether or not the two components are in the same container
     */
    boolean inSameContainer();

    // inSameLifetime

    boolean isInSame(ComponentScope scope);

    /**
     * Returns whether or not the two components are in the same system. Two components are in the same system, iff they
     * have the same {@link ComponentMirror#extensionRoot() system component}.
     * 
     * @return whether or not the two components are in the same system
     * @see ComponentMirror#extensionRoot()
     */
    default boolean isInSameNamespace() {
       throw new UnsupportedOperationException();
    }

    // Just here because it might be easier to remember...
    // isStronglyWired...
    default boolean isStronglyWired() {
        throw new UnsupportedOperationException();
    }

    default boolean isWeaklyWired() {
        throw new UnsupportedOperationException();
    }

    /**
     * The source of the relation.
     * 
     * @return the source of the relation
     */
    ComponentMirror source();

    /**
     * The target of the relation.
     * 
     * @return the target of the relation
     */
    ComponentMirror target();
}
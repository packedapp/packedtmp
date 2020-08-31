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
package app.packed.service.sandbox;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.component.ComponentPath;

/**
 *
 */
interface TreeNode<N> {
    Set<N> children();

    int depth();

    Optional<N> parent();

    void traverse(Consumer<? super N> action);
}

interface NamedTreeNode<N> extends TreeNode<N> {
    String name();

    ComponentPath path();

    // Now that we have parents...
    // add Optional<Component> tryResolve(CharSequence path);
    N resolve(CharSequence path);

    default Optional<N> tryResolve(CharSequence path) {
        throw new UnsupportedOperationException();
    }
}

//configSite()

//hasModifier(ComponentModifier)
//modifiers()
//relationTo(Component)
//stream(Option...)
//viewAs(Object)
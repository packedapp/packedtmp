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

import java.util.Optional;
import java.util.function.Consumer;

import app.packed.component.ComponentStream.Option;

/**
 *
 */
// Ideen er lidt at man kan give saadan en faetter til ting

public interface OldNavigableComponent {
//
//    default Optional<Component> parent() {
//        return tryResolve("..");
//    }
//
//    default Component resolve(CharSequence path) {
//        return tryResolve(path).get();
//    }

    default OldNavigableComponent transform(Object options) {

        // Ideen er lidt at vi kan taege en component
        // Og f.eks. lave den om til en rod...
        // IDK. F.eks. hvis jeg har guests app.
        // Saa vil jeg gerne kunne sige til brugere...
        // Her er en clean Guest... Og du kan ikke se hvad
        // der sker internt...
        throw new UnsupportedOperationException();
    }

    ComponentStream stream(Option... options);

    /**
     * 
     * 
     * <p>
     * This operation does not allocate any objects internally.
     * 
     * @implNote Implementations of this method should never generate object (which is a bit difficult
     * @param action
     *            oops
     */
    // We want to take some options I think. But not as a options
    // Well it is more or less the same options....
    // Tror vi laver options om til en klasse. Og saa har to metoder.
    // Og dropper varargs..
    default void traverse(Consumer<? super Component> action) {
        stream(Option.maxDepth(1)).forEach(action);
    }

    Optional<Component> tryResolve(CharSequence path);
}

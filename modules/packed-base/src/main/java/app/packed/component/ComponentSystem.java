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

import java.util.function.Consumer;

/**
 *
 * <p>
 * In general if you specify a {@link Assembly} to a method that takes a {@link ComponentSystem} you can assume that the
 * method will consume the bundle.
 * 
 * @apiNote In the future, if the Java language permits, {@link ComponentSystem} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
// Sealed, Bundle, Component, ComponentHolder

// Hmm, Vil vi paa et tidspunkt have component.system() ?????
// Saa har vi jo ligesom snuppet et oplagt navn...
public interface ComponentSystem {

    // hmmm, efterhaanden lidt for mange metoder???
    public static void forEach(ComponentSystem s, Consumer<? super Component> action) {
        ComponentStream.of(s).forEach(action);
    }
}

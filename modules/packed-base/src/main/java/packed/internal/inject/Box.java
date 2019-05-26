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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

/** Boxes are the internal representation of a Bundle, Configurator or Host of some kind. */
public final class Box { /* extends Configurable???? */

    /** The service part of the box. */
    private final InjectorBuilder services;

    /** The type of box */
    public final BoxType type;

    /**
     * Creates a new Box.
     * 
     * @param type
     *            the type of box
     */
    public Box(BoxType type) {
        this.type = requireNonNull(type);
        this.services = new InjectorBuilder(type.privateServices());
    }

    /**
     * Returns a service configuration object.
     * 
     * @return a service configuration object
     */
    public InjectorBuilder services() {
        return services;
    }
}

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
package packed.internal.box;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;

import app.packed.bundle.BundleContract;
import packed.internal.classscan.DescriptorFactory;

/** Boxes are the internal representation of a Bundle, Configurator or Host of some kind. */
public final class Box { /* extends Configurable???? */

    /** The lookup object. We default to public access */
    public DescriptorFactory accessor = DescriptorFactory.PUBLIC;

    public BoxHooks hooks;

    /** The service part of the box. */
    private final BoxServices services;

    /** The type of box */
    public final BoxType type;

    /** All direct wirings to other boxes. */
    public final ArrayList<BoxWiring> wirings = new ArrayList<>();

    /**
     * Creates a new Box.
     * 
     * @param type
     *            the type of box
     */
    public Box(BoxType type) {
        this.type = requireNonNull(type);
        this.services = new BoxServices(this);
    }

    /** {@inheritDoc} */
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to use public access (default)");
        // checkConfigurable();
        this.accessor = DescriptorFactory.get(lookup);
    }

    public void buildContract(BundleContract.Builder builder) {
        services().buildContract(builder.services());
    }

    /**
     * Returns a service configuration object.
     * 
     * @return a service configuration object
     */
    public BoxServices services() {
        return services;
    }
}

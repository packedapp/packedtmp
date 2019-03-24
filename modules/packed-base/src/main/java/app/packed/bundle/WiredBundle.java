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
package app.packed.bundle;

import java.util.Set;
import java.util.function.Function;

import app.packed.contract.Contract;
import app.packed.util.Nullable;

/**
 * A bundle that has been wired using {@link Bundle#wire(Bundle, WiringOperation...)} or a similar method.
 * <p>
 * Operations on this object must be performed immediately after {@link Bundle#wire(Bundle)} has been invoked. And
 * before any other operations on the bundle are performed. Failure to do so will result in an
 * {@link IllegalStateException} being thrown from all mutable operations on this object.
 */
public final class WiredBundle {

    WiredBundle(Bundle parent, Bundle child) {

    }

    /**
     * ExportTransient Meaning everything is exported out again from the bundle exportTransient(Filter) Kunne ogsaa vaere
     * paa WiredBundle
     * 
     * @return this bundle
     */
    public WiredBundle exportTransient() {
        return this;
    }

    public WiredBundle exportTransient(Contract contract) {
        return this;
    }

    public WiredBundle exportTransient(Function<Contract, Contract> contract) {
        // We actually would like to able rename services????
        return this;
    }

    /**
     * Returns the set of layers that the bundle is a part of. Are we always part of the main layer????
     * 
     * @return the layers
     */
    public Set<Layer> getLayers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the name of the container that the wired bundle will create. If no name has been set, the runtime will create
     * a name when initializing the container.
     * 
     * @return the name of the container that the wired bundle will create
     * @see #setName(String)
     */
    @Nullable
    public String getName() {
        return "fff";
    }

    /**
     * After or before any transformations??? I think after
     * 
     * @return the incoming contract
     */
    public Contract incoming() {
        throw new UnsupportedOperationException();
    }

    public WiredBundle inLayer(Layer... layers) {
        return this;
    }

    public Contract outgoing() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the name of the container that the wired bundle will create.
     * 
     * @param name
     *            the name of the container that the wired bundle will create
     * @return this instance
     * @throws NullPointerException
     *             if the specified name is null
     * @see #getName()
     */
    public WiredBundle setName(String name) {
        return this;
    }

    /**
     * @return the set of tags on the container. Is is that useful???
     */
    public Set<String> tags() {
        throw new UnsupportedOperationException();
    }

    public void transformIncoming() {

    }

    public void transformOutgoing() {

    }

    /// transform...
}

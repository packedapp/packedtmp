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
package packed.internal.host;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.artifact.ArtifactDriver;
import app.packed.base.ContractSet;
import app.packed.container.ContainerBundle;
import app.packed.container.Wirelet;

/**
 * <p>
 * This interface does not allow to
 */

// Folk bestemmer jo helt selv hvad de vil expose... Men de har en HostContext ihvertfald

// Supportere vi restart
public interface HostContext<T> {

    /**
     * All contracts exposed by the host.
     * 
     * @return all contracts
     */
    ContractSet contracts();

    /**
     * Performs an action for each guest that are deployed on the host.
     * 
     * @param action
     *            the action to perform
     */
    void forEach(Consumer<? extends Guest<T>> action); // Throwable Action???

    <S extends T> void forEach(Consumer<? extends Guest<S>> action, Class<S> artifactType);

    Optional<Guest<T>> get(String name);

    <S extends T> Optional<Guest<S>> get(String name, Class<S> artifactType);

    /**
     * Returns a stream of all the guests that are currently deployed on the host.
     * 
     * @return a stream of all the guests that are currently deployed on the host
     */
    Stream<Guest<T>> guests();

    // Skal vi tillade custom drivers?????
    // Maaske have en default artifact driver
    Guest<T> instantiate(ArtifactDriver<? extends T> driver, ContainerBundle bundle, Wirelet[] userWirelets, Wirelet... hostWirelets);

    /**
     * Returns the number of guests that are currently deployed on the host.
     * 
     * @return the number of guests that are currently deployed on the host
     */
    // Active, non-active
    long size();
}
//HostContext<T> <---?? Why not
//Runtime HostContext....
//Arbejder kun med guests her... Aldrig andet...
//Maaske har en guest ikke en artifact type??? Jooo, den taenker jeg ligger fast
//Men ikke noedvendigvis dens bundle type....

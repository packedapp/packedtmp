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
import app.packed.artifact.ArtifactSource;
import app.packed.base.ContractSet;
import app.packed.container.Wirelet;

/**
 * <p>
 * This interface does not allow to
 */
public interface HostContext<T> {

    /**
     * Performs an action for each guest that are deployed on the host.
     * 
     * @param action
     *            the action to perform
     */
    void forEach(Consumer<? extends Guest<T>> action); // Throwable Action???

    Optional<Guest<T>> get(String name);

    /**
     * Returns a stream of all the guests that are currently deployed on the host.
     * 
     * @return a stream of all the guests that are currently deployed on the host
     */
    Stream<Guest<T>> guests();

    /**
     * Returns the number of guests that are currently deployed on the host.
     * 
     * @return the number of guests that are currently deployed on the host
     */
    long size();

    /**
     * All contracts
     * 
     * @return all contracts
     */
    ContractSet contracts();

    // Skal vi tillade custom drivers?????
    // Maaske have en default artifact driver
    Guest<T> instantiate(ArtifactDriver<? extends T> driver, ArtifactSource assembly, Wirelet[] userWirelets, Wirelet... hostWirelets);
}
//HostContext<T> <---?? Why not
//Runtime HostContext....
//Arbejder kun med guests her... Aldrig andet...
//Maaske har en guest ikke en artifact type??? Jooo, den taenker jeg ligger fast
//Men ikke noedvendigvis dens bundle type....

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

import java.util.stream.Stream;

import app.packed.base.ContractSet;

/**
 *
 */
// Runtime HostContext....
// Arbejder kun med guests her... Aldrig andet...
// Maaske har en guest ikke en artifact type??? Jooo, den taenker jeg ligger fast
// Men ikke noedvendigvis dens bundle type....
public interface HostContext {

    /**
     * All contracts
     * 
     * @return all contracts
     */
    //// Contract of the host... is static
    ContractSet contracts();

    /**
     * Returns a stream of all the guests deployed on this host.
     * 
     * @return a stream of all the guests deployed on this host
     */
    Stream<Guest<?>> guests();

    // If you now for certain that only guest of a particular type has been added
    // Just cast guests();
}

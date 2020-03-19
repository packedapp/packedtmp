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

import app.packed.base.ContractSet;
import app.packed.component.Component;
import app.packed.component.ComponentType;

/**
 * The runtime representation of a host.
 */
public interface Host extends Component {

    /**
     * All contracts
     * 
     * @return all contracts
     */
    ContractSet contracts();

    /** {@inheritDoc} */
    @Override
    default ComponentType type() {
        return ComponentType.HOST;
    }
    // number of guest...
}
// Vi vil gerne af med GuestInstance
// Skal guest vaere side-effect free?,
// Saa man kan returnere den i en stream fra Host...
/// GuestDescriptor (Nah den er live).
// GuestInfo

// Guest = ComponentType???
//// Nope...
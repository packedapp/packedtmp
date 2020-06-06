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
import app.packed.component.ComponentDescriptor;

/**
 * The runtime representation of a host.
 */

// 1 HostType -> Kun X, f.eks. WebSessionHost

// 1 Driver || Manage driveree
// 1 Type || Mange typer
// Isolated || Mesh || Somewhere inbetween

// Restartable | 1-Instance

// 1 Type, Many Types... AppHost, GenericHost...

//HostComponent???
public interface Host extends Component {

    /**
     * All contracts
     * 
     * @return all contracts
     */
    ContractSet contracts();

    /** {@inheritDoc} */
    @Override
    default ComponentDescriptor model() {
        return ComponentDescriptor.COMPONENT_INSTANCE;
    }
}
// Vi vil gerne af med GuestInstance
// Skal guest vaere side-effect free?,
// Saa man kan returnere den i en stream fra Host...
/// GuestDescriptor (Nah den er live).
// GuestInfo

// Guest = ComponentType???
//// Nope...

// Saa mange maader at ekspornere en host paa....
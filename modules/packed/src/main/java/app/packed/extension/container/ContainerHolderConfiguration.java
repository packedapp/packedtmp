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
package app.packed.extension.container;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Wirelet;
import app.packed.extension.bean.BeanHandle;

/**
 *
 */
// Har ikke nogle host objekter. Det er jo en almindelige bean...

// Saa hvis vi endelig ville lave det, skal det generisks for beans.

// Maaske vi bare kan bruge IBC, kan jo altid tage en IBC naar man laver en ContainerInstaller

public class ContainerHolderConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    public ContainerHolderConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    // Problemet er her. At vi saa skal have en ny for en application?
    public ContainerBuilder newInstaller(ContainerTemplate template) {
        throw new UnsupportedOperationException();
    }

    // All guest will have these wirelets
    public ContainerHolderConfiguration<T> addWirelets(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}

//// Nah det er vel bare at capture exceptionen fra lifetime operationen...
//public ContainerGuestConfiguration<T> onFailedInstallation(Consumer<T> t) {
//  return this;
//}

// Guest ->
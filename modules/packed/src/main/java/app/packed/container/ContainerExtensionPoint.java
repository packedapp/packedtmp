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
package app.packed.container;

import app.packed.bean.BeanHandle;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.bridge.ExtensionBridgeBeanConfiguration;
import app.packed.extension.bridge.ExtensionBridgeOuter;

/**
 *
 */
// Uden bean.. -> 

// Med bean

public class ContainerExtensionPoint extends ExtensionPoint<ContainerExtension> {

    /** Creates a new container extension point. */
    ContainerExtensionPoint() {}

    // Hvor faar jeg methodHandle???
    // Important the containerHandle is no longer configurable!!!!
    public ContainerHandle linkNewManyContainer(Assembly assembly, Wirelet[] wirelets, ContainerHandle.InstallOption... options) {
        throw new UnsupportedOperationException();
    }
    
    public ContainerHandle newManyContainer(Wirelet[] wirelets, ContainerHandle.InstallOption... options) {
        throw new UnsupportedOperationException();
    }
    
    // alternativ har vi en speciel bean configuration
    // Hvor man kan registrere companions. Og CWC som en nested interface maaske
    
    // Man skal kunne lave den separate. Lad os sige vi har 4 sessions, hvor vi gerne vil returner det samme
    // Hvis man er lazy bruger man den ikke. Saa den er altid multi
    public <T> ExtensionBridgeBeanConfiguration<T> newContainerWrapper(Class<T> t, ExtensionBridgeOuter... companions) {
        BeanHandle<T> h = null;// extension().use(BeanExtensionPoint.class).newManytonBean(useSite(), t, LifetimeConf.ALL);
        for (ExtensionBridgeOuter clc : companions) {
            System.out.println(clc);
        }
        return new ExtensionBridgeBeanConfiguration<>(h);
    }
}

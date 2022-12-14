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

import app.packed.bean.InstanceBeanConfiguration;

/**
 *
 */

// Vi har brug ContainerInstaller fordi, man ikke konfigure noget efter man har linket
// Saa alt skal goeres inde

// Bliver noedt til at lave et Handle. Da kalderen som minim har brug for
// OperationHandles for lifetimen...

//Ejer

//Support enten linkage(Assembly) or lav en ny XContetainerConfiguration
//Eager, Lazy, ManyTone
//ContainerCompanions (extension configuration)
//Bean <- er taet knyttet til ContainerCompanions
//Hosting (Long term)

public interface ContainerInstaller {

    ContainerInstaller newLifetime();

    ContainerInstaller allowRuntimeWirelets();

    // Only Managed-Operation does not require a wrapper
    default ContainerInstaller wrapIn(InstanceBeanConfiguration<?> wrapperBeanConfiguration) {
        // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum supportere det
        // Hvis vi vil dele den...

        // Det betyder ogsaa vi skal lave en wrapper bean alene
        return null;
    }

    /**
     * <p>
     * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a container handle representing the linked container
     */
    ContainerHandle link(Assembly assembly, Wirelet... wirelets);

    ContainerHandle newContainer(Wirelet... wirelets);
}

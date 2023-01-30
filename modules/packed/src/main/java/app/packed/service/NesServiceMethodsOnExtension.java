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
package app.packed.service;

import java.util.function.Consumer;

/**
 * An extension that deals with the service functionality of a container.
 * <p>
 * This extension provides the following functionality:
 * 
 * is extension provides functionality for exposing and consuming services.
 * 
 * 
 */
//Functionality for
//* Explicitly requiring services: require, requiOpt & Manual Requirements Management
//* Exporting services: export, exportAll

//// Was
// Functionality for
// * Explicitly requiring services: require, requiOpt & Manual Requirements Management
// * Exporting services: export, exportAll
// * Providing components or injectors (provideAll)
// * Manual Injection

// Har ikke behov for delete fra ServiceLocator

// Future potential functionality
/// Contracts
/// Security for public injector.... Maaske skal man explicit lave en public injector???
/// Transient requirements Management (automatic require unresolved services from children)
/// Integration pits
// MHT til Manuel Requirements Management
// (Hmm, lugter vi noget profile?? Nahh, folk maa extende BaseAssembly og vaelge det..
// Hmm saa auto instantiere vi jo injector extensionen
//// Det man gerne vil kunne sige er at hvis InjectorExtensionen er aktiveret. Saa skal man
// altid bruge Manual Requirements
// contracts bliver installeret direkte paa ContainerConfiguration

// Profile virker ikke her. Fordi det er ikke noget man dynamisk vil switche on an off..
// Maybe have an Container.onExtensionActivation(Extension e) <- man kan overskrive....
// Eller @ContainerStuff(onActivation = FooActivator.class) -> ForActivator extends ContainerController

// Taenker den kun bliver aktiveret hvis vi har en factory med mindste 1 unresolved dependency....
// D.v.s. install(Class c) -> aktivere denne extension, hvis der er unresolved dependencies...
// Ellers selvfoelgelig hvis man bruger provide/@Provides\

class NesServiceMethodsOnExtension  {

    /** Create a new service extension. */
    NesServiceMethodsOnExtension() {}

    // Validates the outward facing contract
    public void checkContract(Consumer<? super ServiceContract> validator) {
        // Hmm maaske man ville lave et unit test istedet for...
    }

    public void checkContractExact(ServiceContract sc) {
        // Det der er ved validator ServiceContractChecker er at man kan faa lidt mere context med
        // checkContract(Service(c->c.checkExact(sc));// ContractChecker.exact(sc));
    }

    /**
     * Performs a final transformation of any exported service.
     * 
     * This method can perform any final adjustments of services before they are made available to any parent container.
     * <p>
     * The transformation takes place xxxx
     * 
     * @param transformer
     *            transforms the exported services
     */
}

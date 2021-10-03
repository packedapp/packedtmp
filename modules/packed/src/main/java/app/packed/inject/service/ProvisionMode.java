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
package app.packed.inject.service;

// ? Why is ProvisionMode not part of ServiceContract
// A Because it should not be part of how services are used

public enum ProvisionMode {
    ON_DEMAND, EAGER_SINGLETON, LAZY_SINGLETON;
    
    // @ImplNote
    
    //// The runtime will rarely be able to store singletons more efficent
    
    //// It a lot a situation it tempting to use either of the constant modes.
    //// However, you might be better of using ON_DEMAND
    
    //// A eager constant is typically stored in an array.
    //// So primitive types are stored as their wrapper types
    
    // LazyConstant are typically stored in wrapper object with a single volatile field
}

// Rename to prototype
// Maaske supportere vi ikke konstanter... Og saa har en clean @Provide...
// Saa kan den ogsaa bruges fra hooks
/// Og hvad er constant? LAZY or EAGER?
//// Saa vi har vel tre modes - ON_DEMAND, LAZY_SCOPED_SINGLETON, EAGER_SCOPED_SINGLETON
/// EAGER_CONSTANT, LAZY_CONSTANT
//// Med scoped mener vi at det ikke er en global singleton


// Saa har vi lige den med ExtensionMember ogsaa...
//// Maaske har man noget @Provide.defaultExtension = ServiceExtension.class
//// Forstaaet paa den maade at man bruger ServiceExtension som default

//@Provide(Constant) , @Provide(Lazy), @Provide(EVERYTIME) -> Constant early, Lazy (volatile storage) <-- kan jo godt skrive laese volatile i et object array
//serviceProvide? provideServicee
//ProvideService

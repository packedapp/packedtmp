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
package app.packed.lifetime;

/**
 *
 */
// LifetimeManagement bare?
// Maaske ender det bare med en boolean
public enum LifetimeKind {
    
    STATELESS,

    UNMANAGED, // has initialize

    MANAGED; // has start/stop
}
//
//enum AlternativeNaming {
//    STATIC,
//
//    UNMANAGED_INSTANCE,
//
//    MANAGED_INSTANCE
//}
//
//// Functional+ Static does not have a lifetime. dvs Optional paa alle componenter
//enum AlternativeNaming2 {
//    NONE, 
//
//    UNMANAGED,
//
//    MANAGED;
//}

// An Unmanaged Lifetime must not have active threads running inside of it
// post startup

// An Unmanaged lifetime will never have Managed lifetime as children

// Det her betyder at Injector har en Unmanaged Lifetime.
// Og App har en Managed Lifetime

// Unmanaged Lifetime typically relies on the garbage collection to clean up things.
// Cleaner can be used. But 

// Or Tracked vs Untracked

// Unmanaged it is created, but then no longer tracked...

// Applications that are Managed should generally be AutoCloseable
// Or managed by an application host
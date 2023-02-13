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

// Deprecated????

//We had stateless once... because of functional beans
//But now they are in the same lifetime as the container in which they are registered.
//Because even though they are stateless they should be called outside of the containers
//lifetime

// Tror vi skal have Stateless introduceret igen
// Og så sige at

public enum LifetimeKind {

    /**
     * Represents a bean or container who either have no run-state (static) or whose run-state is not tracked by any
     * extension.
     * <p>
     * Beans that have stateless lifetime. Will be contained in the container's lifetime. Meaning that they can be used as
     * long as the container they are in are running.
     *
     * @see app.packed.bean.BeanKind#STATIC
     * @see app.packed.bean.BeanKind#FOREIGN
     **/
    STATELESS,

    /**
     * A lifetime that can only be initialized. But where there exists no way to close it. Cleanup relies on the garbage
     * collection to clean up.
     * <p>
     * A typical example is prototype services. Once created they are no longer tracked.
     *
     * Cleanup must be done by the garbage collector or explicit by the user when it is no longer in used.
     *
     * <p>
     * Once a bean with unmanaged scope has been created. It is no longer tracked
     * <p>
     * Unmanaged beans cannot be started or stopped.
     **/
    UNMANAGED, // has initialize

    /**
     *
     * Managed bean can only be registered in a managed container. Installing a managed bean in an unmanaged container will
     * fail with UnmanagedLifetimeException at build time.
     **/
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
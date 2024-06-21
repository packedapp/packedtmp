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
 * The framework operates with three different kinds of lifecycle (models):
 *
 * None: Either the component does not have an instance, for example, a static bean. Or the lifecycle of the instance is
 * completely managed outside of the application, from construction to destruction.
 *
 * Unmanaged: A lifetime kind that is created (at least partially created) by the framework, but destroyed by the GC or
 * by someone outside of the application.
 *
 * Managed: A lifetime that must be destroyed explicitly. One caveat, a bean may be created outside of the application.
 * But then "touched" at some time doing the construction phase.
 *
 * Unmanaged is a HashMap, Managed is something you can close (A resource is an object that must be closed after the
 * program is finished with it.) https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
 * <p>
 * <ul>
 * <li>An unmanaged lifetime cannot explicitly be destroyed, instead it relies on the GC to clean it up.</li>
 * <li>An unmanaged lifetime has no active threads running besides for the thread that creates the lifetime. Outside can
 * call in though</li>
 * <li>The state of an unmanaged lifetime is never directly observable.</li>
 * <li>Beans in an unmanaged lifetime never goes through a starting or stopping phase. Trying to use annotations such as
 * {@link app.packed.bean.OnStart} or {@link app.packed.bean.OnStart} on a bean in an unmanaged lifetime will fail at
 * build time</li>
 * <li>It is unitialized to initiazling to initialzized</li>
 * </ul>
 *
 *
 */

// Unmanaged Container - Unmanaged Application
// Unmanaged Container - Managed Application (Det er vel det en bootstrap app laver)
// Unmanaged Container - Unmanaged Container (OK)
// Unmanaged Container - Managed Container (?)
// Unmanaged Container - Unmanaged Bean (OK)
// Unmanaged Container - Managed Bean (FAIL)

// LifetimeManagement bare?
// Maaske ender det bare med en boolean

// Deprecated????

//We had stateless once... because of functional beans
//But now they are in the same lifetime as the container in which they are registered.
//Because even though they are stateless they should be called outside of the containers
//lifetime

// Bean vs Container er ogsaa en slags Kind

// NONE, CREATIONAL, DESTRUCTIBLE
// NONE, UNMANAGED, MANAGED

public enum LifecycleKind {

    NONE,

    /**
     * Represents a bean or container who either have no run-state (static) or whose run-state is not tracked by the
     * runtime. A lifetime that can only be initialized and not stopped. Cleanup relies only on the garbage collector.
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

///**
//* Represents a bean or container who either have no run-state (static) or whose run-state is not tracked by any
//* extension.
//* <p>
//* Beans that have stateless lifetime. Will be contained in the container's lifetime. Meaning that they can be used as
//* long as the container they are installed in is running.
//*
//* @see app.packed.bean.BeanKind#STATIC
//* @see app.packed.bean.BeanKind#FOREIGN
//**/
//STATELESS,

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
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
package app.packed.lifecycle;

/**
 * There are 3 different supported lifecycle models for beans in this framework:
 *
 * None: Either the bean does not have an instance, for example, a static bean. Or the lifecycle of the bean instance is
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
 * <li>An unmanaged bean cannot explicitly be destroyed, instead it relies on the GC to clean it up.</li>
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
public enum LifecycleKind {

    /** A bean that has no lifecycle. */
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

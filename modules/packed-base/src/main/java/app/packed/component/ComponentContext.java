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
package app.packed.component;

import app.packed.inject.Injector;

/**
 * A component context can be injected into what every I call it...
 */
// Skal vi droppe at extende component???
//// Igen er bange for at man kommer til at implementere begge dele.
// Hvis vi skal have noget some helst kontrol...
// Boer vi ikke extende component, da vi risikiere
// At returnere en instand i ComponentStream.
public interface ComponentContext extends Component {
    // PrototypeContext

    // install(); <- installs child
    // spawn();
    // spawn(100);

    // Error handling top->down and then as a static bundle method as last resort.
    // The bundle XX.... defines a non-static error handler method. But it was never installed

    // S spawn();
    // CompletableFuture<S> spawnAsync();

    // NU ER VI TILBAGE MED EN COMPONENT KAN HAVE EN ROLLE... (eller flere???)

    // Role == ENUM?????

    // Role = Container.class , type = DefaultContainer
    // Role = Actor.class, type = SomeActor
    // Role = Host.class, type = SomeHostImplementation?
    // Role = Component.class, type = SomeComponent
    // Role = Pooled.class, type = SomeElement.class
    // Role = Singleton.class, type = ddddd
    // Role = Statics.class, type = DDDDD
    // Role = RunInScope.class, type = DDDDD
    // Role = Unmanaged.class, type = DDDDD (Used with prototype services)
    // Role = ActorSystem.class [holds actors]
    // Role = Class, implementation = Class
    // Role == Class only if users can define their own role....
    // Role == JobManager.class
    // Role == Job.class
    // Role == ScheduledJob.class <- Is it a component????

    // Stateless, Statefull, DistributedObject, Entity <-

    // Requestlets, Scopelets, ...

    // Charactariska = How Many Instances, Managaged/ Unmanaged, Dynamic-wire (host)

    // Wirelets for components??????? Nej ikke udo
    // install(Doo.class, name("fsdsfd"), description("weweqw));

    // install(Role, implementation, wirelets);

    // Bundle.setDefaultRole <- On Runtime.
    // F.eks. Actor .withRole(Actor)

    // Role -> Pool [5-25 instance, timeout 1 minute]

    // I role skulle man kun installere en slags controller...

    // Install

    // setMaxInstances();

    // Role-> PrototypeOptionas. Its a prototype of

    // I think there are extra settings on prototype...
    // Such as caching...
    // Because they are unthogonal, lazy has nothing todo with actors.

    // But about runtime hotswap, for example, for actors...
    // We kind of swap the type...

    // We have a special component implementation for that...

    /**
     * Returns the private injector of this component.
     *
     * @return the private injector of this component
     */
    // Privatesss?????Syntes skal hedde det samme, Bliver maaske lazy initialiseret efter startup
    // Maaske skal vi bare extende Injector.....

    Injector injector();// sidecar???

    // Container
}

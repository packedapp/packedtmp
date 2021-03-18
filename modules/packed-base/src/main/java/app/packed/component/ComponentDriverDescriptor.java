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

/**
 *
 */
// Let ExtensionDescriptor extend it???
// Nah
// Grunden til vi splitter det op er egenlig at vi tillader private
// component drivere. Saa hvis vi gerne vil have
// ComponentDriverDescriptor Component.driver();
// og ikke Component.driver();

public interface ComponentDriverDescriptor {

    static ComponentDriverDescriptor of(Assembly<?> assembly) {
        throw new UnsupportedOperationException();
    }
}
//TODO vi kunne expose noget om hvad den laver den her driver...
//Saa kan man lave checks via ContainerConfigurationContext
//sourceModel.doo().hasAnnotation(Actor.class);
//Error handling top->down and then as a static container method as last resort.
//The container XX.... defines a non-static error handler method. But it was never installed

//S spawn();
//CompletableFuture<S> spawnAsync();

//Stateless, Statefull, DistributedObject, Entity <-

//Requestlets, Scopelets, ...

//Charactariska = How Many Instances, Managaged/ Unmanaged, Dynamic-wire (host)

//Wirelets for components??????? Nej ikke udo
//install(Doo.class, name("fsdsfd"), description("weweqw));

//install(Role, implementation, wirelets);

//assembly.setDefaultRole <- On Runtime.
//F.eks. Actor .withRole(Actor)

//Role -> Pool [5-25 instance, timeout 1 minute]

//I role skulle man kun installere en slags controller...

//Install

//setMaxInstances();

//Role-> PrototypeOptionas. Its a prototype of

//I think there are extra settings on prototype...
//Such as caching...
//Because they are unthogonal, lazy has nothing todo with actors.

//But about runtime hotswap, for example, for actors...
//We kind of swap the type...

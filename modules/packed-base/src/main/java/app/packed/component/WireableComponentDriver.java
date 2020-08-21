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

import java.lang.invoke.MethodHandles;
import java.util.Optional;

/**
 * 
 * 
 * @param <C>
 *            the type of configuration the driver expose to users for configuring the underlying component
 * 
 * @apiNote In the future, if the Java language permits, {@link WireableComponentDriver} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
// TODO maybe remove all methods...
// And just retain implementations for internal usage...
public interface WireableComponentDriver<C> extends ComponentDriver<C> {

    default Optional<Class<?>> source() {
        throw new UnsupportedOperationException();
    }

    static <C> WireableComponentDriver<C> create(MethodHandles.Lookup lookup, Option... options) {
        throw new UnsupportedOperationException();
    }

}

// Error handling top->down and then as a static bundle method as last resort.
// The bundle XX.... defines a non-static error handler method. But it was never installed

// S spawn();
// CompletableFuture<S> spawnAsync();

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

// COMPONENT_DRIVEr definere ingen drivere selv..
// Skal den vaere here eller paa ComponentDriver????
// Syntes egentlig ikke den er tilknyttet ComponentDriver...
// Men hvis folk selv definere for custom defineret vil det maaske give mening.
// At smide dem paa configurationen... Der er jo ingen

// En anden meget positiv ting er at vi vi har 2 component drivere
// sourced and unsourced. People shouldn't really need to look at
// both of the classes two find what they need..

//interface InstanDriver<T, C>

/**
 *
 *
 * @param <C>
 *            the type of configuration that will be returned to the user
 */
//Noget med scanning....

//Det er jo fucking genialt...
//Det betyder folk kan lave deres egne "component" systemer...

//Vi supporter kun en surragate taenker jeg
//Vi supportere ogsaa kun klasse scanning paa registrering tidspunkt.
//Ikke frit dynamisk taenker jeg. 

//Altsaa er det her maaden at lave prototype service paa???

//Vi vil forresten gerne have en SingletonContext der ogsaa fungere for Funktioner med registrere...
//Nej de har jo ikke en type....
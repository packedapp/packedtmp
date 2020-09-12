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
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import app.packed.base.TypeLiteral;
import packed.internal.component.PackedComponentDriver;

/**
 * 
 * @param <C>
 *            the type of configuration the driver expose to users for configuring the underlying component
 * 
 * @apiNote In the future, if the Java language permits, {@link ComponentDriver} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
// TODO maybe remove all methods... And have attributes???
// And just retain implementations for internal usage...
public interface ComponentDriver<C> {

    // boolean isBound() <--- bound to a source

    default Optional<Class<?>> source() {
        return Optional.empty();
    }

    static <C> ComponentDriver<C> of(MethodHandles.Lookup lookup, Class<? extends C> driverType, Option... options) {
        return PackedComponentDriver.of(lookup, driverType, options);
    }

    static <C> ComponentDriver<C> of(MethodHandles.Lookup lookup, TypeLiteral<? extends C> driverType, Option... options) {
        throw new UnsupportedOperationException();
    }

    // The runtime may add further attributes when applying this driver.
    // For example, the root component of a system always has the SYSTEM property.
    // Irrecspectively of the component driver that was used
    // Hmm ved ikke om vi vil have den..
    ComponentModifierSet modifiers();

    /**
     *
     * @apiNote In the future, if the Java language permits, {@link ShellDriver} may become a {@code sealed} interface,
     *          which would prohibit subclassing except by explicitly permitted types.
     */
    public interface Option {

        static Option sourceAssignableTo(Class<?> rawType) {
            throw new UnsupportedOperationException();
        }

        /**
         * The component the driver will be a container.
         * <p>
         * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
         * 
         * @return stuff
         * @see ComponentModifier#CONTAINER
         */
        static Option container() {
            return PackedComponentDriver.OptionImpl.CONTAINER;
        }

        /**
         * The component the driver will be a container.
         * <p>
         * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
         * 
         * @return stuff
         * @see ComponentModifier#CONSTANT
         */
        // InstanceComponentDriver automatically sets the source...
        static Option constantSource() {
            return PackedComponentDriver.OptionImpl.CONSTANT;
        }

        // The parent + the driver
        //
        static Option validateWiring(BiConsumer<Component, ComponentDriver<?>> validator) {
            throw new UnsupportedOperationException();
        }

        static Option validateParentIsContainer() {
            return validateParent(c -> c.hasModifier(ComponentModifier.CONTAINER), "This component can only be wired to a container");
        }

        static Option validateParent(Predicate<? super Component> validator, String msg) {
            return validateWiring((c, d) -> {
                if (validator.test(c)) {
                    throw new IllegalArgumentException(msg);
                }
            });
        }

        // Option serviceable()
        // Hmm Maaske er alle serviceable.. Og man maa bare lade vaere
        // at expose funktionaliteten.
    }
}

//Error handling top->down and then as a static bundle method as last resort.
//The bundle XX.... defines a non-static error handler method. But it was never installed

//S spawn();
//CompletableFuture<S> spawnAsync();

//Stateless, Statefull, DistributedObject, Entity <-

//Requestlets, Scopelets, ...

//Charactariska = How Many Instances, Managaged/ Unmanaged, Dynamic-wire (host)

//Wirelets for components??????? Nej ikke udo
//install(Doo.class, name("fsdsfd"), description("weweqw));

//install(Role, implementation, wirelets);

//Bundle.setDefaultRole <- On Runtime.
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

//We have a special component implementation for that...

//COMPONENT_DRIVEr definere ingen drivere selv..
//Skal den vaere here eller paa ComponentDriver????
//Syntes egentlig ikke den er tilknyttet ComponentDriver...
//Men hvis folk selv definere for custom defineret vil det maaske give mening.
//At smide dem paa configurationen... Der er jo ingen

//En anden meget positiv ting er at vi vi har 2 component drivere
//sourced and unsourced. People shouldn't really need to look at
//both of the classes two find what they need..

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

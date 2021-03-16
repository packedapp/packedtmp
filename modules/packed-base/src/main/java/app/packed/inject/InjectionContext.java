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
package app.packed.inject;

import java.util.List;

import app.packed.base.Key;
import app.packed.base.Variable;

/**
 * An injection context object can be injected into any injection site (typically a method or constructor) to query
 * about what dependencies are available for injection at the particular injection site.
 * <p>
 * Example
 * <p>
 * May change from type to type even when registered in the same container.
 * <p>
 * we both need to check the type and the qualifier which must be readable by #visibility
 * <p>
 * <strong>Note:</strong> This class should only be used doing development. As the runtime needs to retain detailed
 * information about the dependency graph that is normally only available at build time.
 */
public /* sealed */  interface InjectionContext extends ServiceRegistry {


    default void printDependencyTree() {
        // Det er jo bare en trae af ServiceDependency

        // ResolvedVariable -> Status Unresolved but Optional.

        // InjectableDependency?

        // En for hver parameter...
//        com.javadeveloperzone:maven-show-dependency-tree:jar:1.0-SNAPSHOT
//        [INFO] \- org.springframework.boot:spring-boot-devtools:jar:1.5.4.RELEASE:compile
//        [INFO]    +- org.springframework.boot:spring-boot:jar:1.5.4.RELEASE:compile
//        [INFO]    |  +- org.springframework:spring-core:jar:4.3.9.RELEASE:compile
//        [INFO]    |  |  \- commons-logging:commons-logging:jar:1.2:compile
//        [INFO]    |  \- org.springframework:spring-context:jar:4.3.9.RELEASE:compile
//        [INFO]    |     +- org.springframework:spring-aop:jar:4.3.9.RELEASE:compile
//        [INFO]    |     +- org.springframework:spring-beans:jar:4.3.9.RELEASE:compile
//        [INFO]    |     \- org.springframework:spring-expression:jar:4.3.9.RELEASE:compile
//        [INFO]    \- org.springframework.boot:spring-boot-autoconfigure:jar:1.5.4.RELEASE:compile
    }

    /**
     * Returns the class that was used to determined which keys are available for injection.
     * 
     * For example, a service that is registered with a package private {@link Key}. Will only be in the set of keys
     * returned by {@link #keys()} if the class returned by this method is in the same package.
     * <p>
     * Normally the declaring class of the constructor or method to who this context is injected into is returned. However,
     * if interface is used from a composite class. The class that depends on the composite will be returned.
     * 
     * @return the class for which we calculate visibility
     */
    // contextTarget target()
    // Eller kan man lave noget hullumhej her ved at lade publish nogle composite klasser, med package private
    // constructors...
    // Og som tager hemmelige "klasser".. Nej man skal ikke kunne instantiere en eller anden composite, som man ikke har
    // adgang til... Men
    Class<?> targetClass(); // Det er jo bare .getClass(); Med mindre det er en Composite...

    default List<Variable> variables() {
        throw new UnsupportedOperationException();
    }
}

// Set<Class<Context>> contextTypes();

// ServiceTrace trace(SomeKey<?>); // Angiver totalt hvordan den service ankommer...
// Alternativt er at faa injected en speciel type.. Men naeh taenker det her er lettest.
// Igen.. Hops after Hops... Fra den hvor den er registreret her, og saa en mapper.. og fra den her config fil...

///**
// * An immutable set of keys for which dependencies are available for injection at the injection site (typically a method
// * or constructor annotated with {@link Inject}).
// * <p>
// * Note: Even though The set of keys returned by this method does not take into consideration that depending on them
// * will lead to cycles in the dependency graph.
// * 
// * @return an immutable set of keys for services that are available for injection at the injection site
// */
//// maybe this is more context. And the use a ServiceRegistry to get services
//// The returned set does not include services that are available...
//// Inject an instance of ServiceRegistry to see those...
//Set<Key<?>> keys();

// Something about layers....
// If you have 100 services.... And a Method hook adds 3 services...
// There can be really difficult to find...
// So some kind of layering... Maybe it can be feed back to service layer thingy.
// Because it is kind of layer which can only be used from the particular service.

// Set<Class<? extends Annotation>> Factory.primers()
//// Primers can be resolved in relationship to a container
//// Primers can use normal optional/nullable/composite stuff... or they can ignore it... The contract is really
// A textual one, and not a programmatic one....
// Maybe you can opt.. support Optional, Nullable, NotLazy, Provider, Not Composite
// Paa PrimeProvider
// No validation is performed because we can always override the default.... So there is just no way to do it....

// So Factory -> Set<Key<?>> requirements, Set<Key<?>> optionalRequirements, Set<Class<?>> primers()... 

/// ---------------
// Map<String, String> filtered(); A map of all filtered types.... For example, because there are not visible...
// Value is description...

//Er det bare en Sidecar der tager en InjectionRuntime+ComponentContext???
//ComponentContext... Skal ogsaa styres via Access.. @InjectContext(needs ComponentContext)...
//Naah, component burde da vaere fint... Den kan alle faa

//Nej den er speciel... Alle kan supportere den paa en eller anden maade...

//Man laver vel en skabelon naar man gerne vil finde bruge den...

//Vi ved ikke noget om primeannotation

//Inject context kender ikke noget til prime annotations.
//De styrer deres eget loeb, men det anbefales at de overholder f.eks. @Nullable

//En InjectContext vil aldrig dukke op som et requirement
// Can we cheat with Composite here? I think maybe....
// Not first priority though...
//// Det ville ikke fungere super godt hvis vi bruger en sidecar...

// Altsaa der er jo nogen gange hvor ting ikke er tilgaengelig for en constructor. Men kun en metode??

//@Stuff Set<Key<?>> thisIsThing
//Provided via @Stuff + InjectionContext -> ic.keys();
///// -----------
// Vi kunne jo tit godt taenke os at fortaelle en historie...
// Du kan bruge den her service fordi du er annoteret med @Actor...
//// ConfigSite med @Actor som parent???
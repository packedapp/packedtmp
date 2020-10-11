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
package app.packed.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.component.ComponentRelation;
import app.packed.inject.Provide;
import app.packed.sidecar.ActivateMethodSidecar;
import app.packed.sidecar.MethodSidecar;

/**
 * A class cannot define more than one method annotated with {@link ConnectExtension}.
 * 
 * And allows extensions to connect across container boundaries both at build-time and at runtime.
 * 
 * <p>
 * This annotation can be used on subclasses of {@link Extension} or any singleton services that is annotated with
 * {@link ExtensionMember}. In which case the extension can be injected
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

// Problemet er lidt sidecars.... Hvorfor???

// Giver det mening at have en for components???? Maaske bare en alm listener???
// 
@ActivateMethodSidecar(allowInvoke = true, sidecar = MethodSidecar.class)
public @interface ConnectExtension {

    // Only children not anything farther removed...
    // If not only direct children. Only the closest ancestor will have its
    // method invoked.

    // onlyDirectAccenden
    // onlyChild
    // immediateChildOnly

    // Maaske bare drop den. og lav en conditional via context.isChild()
    // Maaske i foerste omgang... Vi har brug for context taenker jeg...
    boolean onlyDirectLink() default false;// onlyDirectLink

    // boolean crossArtifacts default ???
}

class Sidecar extends MethodSidecar {

    @Provide
    ComponentRelation cr() {
        throw new UnsupportedOperationException();
    }
}

// Altsaa det fx. hvis vi ikke har naaet at starte... 
interface Unlinkable {

    // Invoked by the runtime
    void failed(Throwable t);
}

//Or ExtensionWired if linked=strong, ... onExtensionLinked  (Separate naming for lifecycle and events at,on,post,pre, when)

//Er den generisk?? Hvad med runtime??? Vi skal jo paa en eller anden maade have fat i en webserver...

//Maaske en WebServerExtensionRuntime kan have baade en for configuration time and start time...

//@Connect @ConnectWith, @OnConnected, @OnWired, @ExtensionLinked

//Linking->Static...
//@ExtensionWired

//ComponentWiring (How the two components are related)
//In this case the two containers that are linked

//Distance/Strong/Weakly linked
// Can be injected together with the actual extension...
// And with Any extension wirelets specified for the child...
// So @WireletSupply will override what the actual parent does...

// Det kan ogsaa vaere vi laver noget generisk her..
// I virkeligheden er det jo bare en ContainerRelation (ikke en component relation)
//interface ExtensionLinkedContext {
//
//    default boolean isChild() {
//        return distance() == 1;
//    }
//
//    // isInSameArticact
//
//    int distance();
//}

// ServiceExtension bliver noedt til at have

// Injector parentInjector
// ServiceExtension parent (eller maaske bare children...). Nej fordi vi 
// skal starte med at kalkulere naar vi ikke har en extension parent...

// ServiceExtension()

// Can be returned by a method annotated with DescendentAdded...
// Iff they maintain a reference to the child...
// Otherwise we don't care at all. It can 

///// Okay so service mesh......
///// Skal vi have en Pod integration paa en eller anden maade....
///// Vi deployer Customer, Billing, Order med en eller anden dependency graph...

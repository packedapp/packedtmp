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
package internal.app.packed.lifetime.sandbox2;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import app.packed.bean.BeanMirror;
import app.packed.bindings.BindingMirror;
import app.packed.lifetime.LifetimeMirror;
import app.packed.lifetime.sandbox.LifetimeOperationMirror;

// Why lifetime bean
// Det startede lidt med problemet omkring Application "wrapper" objekter som AsyncApp/Injector osv.
// Det er jo basalt set en bean, andet giver naesten ikke mening. Isaer naar vi skal have App on App
// Dette ledte til at vi selvfoelgelig skal kunne mirror den. Hvilket foerte til denne bean.
// Eftersom operationer der laver en application ikke kan vaere definere indefor selve applicationer.
// Da det ville bryde encapsulation. Ligger vi disse operationer udenfor application. Hvilket skaber
// en ny bean (LifetimeBean) og en ny container og en ny bootstrap application


// Q: Hvorfor ikke for bean
// A: Vi supportere ikke rigtig nogle companion objects for beans.
// Det eneste vi har er en managed state. Som folk selv lidt maa styre...

/**
 * A bean that manages one or more lifetime instances.
 * <p>
 * All lifetimes are managed by a bean of this type except for the lifetime of a bootstrap application. Where
 * {@link LifetimeMirror#base()} return empty.
 * <p>
 * A lifetime management bean typically only manages lifetime.
 * <p>
 * The bean lives and dies with the instance
 */
// Any non-stateless lifetime has a "managing bean" that creates and destroys it.
// Alle components der ikke er stateless har en LifetimeBean

// A stateless lifetime component does not have lifetime bean...
//

// A lifetime bean may create more than 1 type of lifetime

// Har vi brug for den??? Giver det mening at vi altid har en bean??
// Giver det ikke mere mening at det fx er ExtensionBean der laver en
// WebExtension????

// Operator = owner -> Self managing operation

//LifetimeWrapperBean

// A bean that is specifically created to hold container lifetime thingies
// Er ikke sikker paa vi skal have den... Det er jo foerst og fremmest en syntetisk
// bean...

@Deprecated
class ContainerLifetimeBeanMirror extends BeanMirror {

    /** {@return a collection of the lifetimes managed by this bean.} */
    // Hmm, skal den her paa BeanMirror???
    
    // I virkeligheden er det jo ikke InjectorImpl der styre creation af den lifetime
    // Men Bootstrap appen. InjectorImpl holder bare noget info...
    public Collection<LifetimeMirror> managesLifetimes() {
        throw new UnsupportedOperationException();
    }

    ///// Resten er lidt ligegyldigt.
    /// Kan ikke se hvorfor man ikke kan managed lifetimes af forskellige typer
    // Og den sidste er bare selectOperationsOfType(LifetimeOperationMirror.class);
    // Skal have nogle bedre navne end managed


    /**
     * Returns a non-empty list of all the lifetime management operations this bean provide.
     * <p>
     * The first operation in the returned list is always the launch operation.
     * 
     * @return a list of all lifetime management operations this bean provides
     */
    public List<LifetimeOperationMirror> managementOperations() {
        throw new UnsupportedOperationException();
    }
}

interface Zandbox1 {

    // Fails for bootstrap????
    // Operations that are particular to the launch of the launchesLifetime

    // First operation is always the launch operation
    // BootstrapLifetime does not have lifetime management operation mirror

    // All lifetimes

    // isSynthetic

    // Hmm
    // LifetimeHolderBeanMirror vs LifetimeGuestBeanMirror

    // Uhhh hvordan er den defineret for en root applikation???
    // Den er jo ikke en del af applikationer, eller maaske er den...

    // Maaske er det ikke noedve
    /// LifetimeHolderBeanMirror extends LifetimeHolderMirror? Hmm
    // Saa skal vi til at caste...

    ///// Hmm har vi Application/Container/Bean specialization?

    // ExposedAs??? ExportedAs??
    // Optional<Class<?>> exposedAsWrapper();
    // SessionContext, FooBean, Nothing, AsyncApp
    // IDK about this

//  default LifetimeOperationMirror createdBy() {
//  // operations().get(0); Det er ikke en operation i den launched lifetime...
//  return launchesLifetime().operations().get(0);
//}

// Launches this lifetime
// Hvis vi har statiske images, kan vi launche flere forskellige lifetimes
// Bootstrap laver altid kun 1 type

// Taenker de maa alle tage de sammen launch contexts...

    public class ContainerLifetimeLaunchBeanMirror /* extends LifetimeBeanMirror */ {

        // Bridge dependencies...

    }

    // All managed lifetime will have the same contexts available
    List<Object> managedContexts();

    /**
     * Returns a non-empty list of dependencies for this lifetime holder.
     * 
     * @return a list of dependencies for this lifetime holder
     */
    // Er jo ogsaa paa
    List<BindingMirror> dependencies();

    // er jo beanClass nu
    Class<?> holderClass();

    Optional<LifetimeHostBeanMirror> host();

    // Tror ikke vi har en generisk Host bean
    public class LifetimeHostBeanMirror extends BeanMirror {

        // Som regel er den her en LifetimeHolder. Men ikke altid
        // Eller maaske skal det bare vaere altid
        public BeanMirror holds() {
            throw new UnsupportedOperationException();
        }
    }

}

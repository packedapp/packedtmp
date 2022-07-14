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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import app.packed.lifetime.sandbox.LifetimeHostBeanMirror;
import app.packed.operation.dependency.DependencyMirror;

/**
 * A bean that manages one or more lifetime instances.
 * <p>
 * All lifetimes are managed by a bean of this type except for the lifetime of a bootstrap application. Where
 * {@link LifetimeMirror#managedBy()} return empty.
 * <p>
 * A lifetime management bean typically only manages lifetime.
 * <p>
 * The bean lives and dies with the instance
 */
// Alle components der ikke er stateless har en LifetimeBean
public interface LifetimeBeanMirror {
    
    default LifetimeKind managesLifetimeKind() {
        return managesLifetimes().iterator().next().lifetimeKind();
    }

    /** {@return a collection of the lifetimes managed by this bean.} */
    Collection<LifetimeMirror> managesLifetimes();

    /**
     * Returns a non-empty list of all the lifetime management operations this bean provide.
     * <p>
     * The first operation in the returned list is always the launch operation.
     * 
     * @return a list of all lifetime management operations this bean provides
     */
    List<LifetimeOperationMirror> managementOperations();
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

    public interface ContainerLifetimeLaunchBeanMirror extends LifetimeBeanMirror {

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
    List<DependencyMirror> dependencies();

    // er jo beanClass nu
    Class<?> holderClass();

    Optional<LifetimeHostBeanMirror> host();
}

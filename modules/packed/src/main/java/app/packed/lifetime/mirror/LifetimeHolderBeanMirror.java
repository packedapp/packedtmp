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
package app.packed.lifetime.mirror;

import java.util.List;
import java.util.Optional;

import app.packed.bean.BeanKind;
import app.packed.lifetime.sandbox.LifetimeHostBeanMirror;
import app.packed.operation.dependency.DependencyMirror;

/**
 * Holds a single managed lifetime instance.
 * <p>
 * Is always of type {@link BeanKind#MANAGED} (as long as we still have that enum).
 * <p>
 * The bean lives and dies with the instance
 */
// isSynthetic

// Hmm
// LifetimeHolderBeanMirror vs LifetimeGuestBeanMirror

// Uhhh hvordan er den defineret for en root applikation???
// Den er jo ikke en del af applikationer, eller maaske er den...

// Maaske er det ikke noedve
/// LifetimeHolderBeanMirror extends LifetimeHolderMirror? Hmm
//Saa skal vi til at caste...

///// Hmm har vi Application/Container/Bean specialization?
public interface LifetimeHolderBeanMirror {

    default LifetimeOperationMirror createdBy() {
        return holderOfLifetime().operations().get(0);
    }

    LifetimeMirror holderOfLifetime();

    Optional<LifetimeHostBeanMirror> host();

    // ExposedAs??? ExportedAs??
    // Optional<Class<?>> exposedAsWrapper();
    // SessionContext, FooBean, Nothing, AsyncApp
    // IDK about this
}

interface Zandbox1 {


    /**
     * Returns a non-empty list of dependencies for this lifetime holder.
     * 
     * @return a list of dependencies for this lifetime holder
     */
    // Er jo ogsaa paa
    List<DependencyMirror> dependencies();
    
    // er jo beanClass  nu
    Class<?> holderClass();
}

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
package app.packed.operation.bindings.sandbox;

import java.util.Optional;

import app.packed.container.UserOrExtension;
import app.packed.operation.OperationMirror;
import app.packed.operation.Variable;
import internal.app.packed.container.Mirror;

/**
 *
 */
public interface Binding2Mirror extends Mirror {

    Optional<DefaultMirror> fallback(); // Do we parse it even if we have been build-time resolved????
    
    // Resolved
    // Unresolved + [Optional|Default]
    // RuntimeResolvable
    // Composite -> composite.all.isSatisfiable

    ResolutionState resolutionState();

    
    BindingKind bindingKind();

    // Unresolved->Empty or Composite->Empty
    Optional<UserOrExtension> resolvedBy();

    Variable variableStripped(); // Remove @Nullable Quaifiers, Optional, PrimeAnnotation ect.. All annotations?? Maaske er det bare en type
    
    /**
     * If this dependency is the result of another operation.
     * 
     * @return
     */
    // Talking about some caching here makes sense
    // ConstantPoolReadOperationMirror
    // Er empty if Unesolved... Eller hvad.
    // Hvis vi fx er @Default ville vi vel gerne have nogle fake operationer

    // Altsaa den ikke super meget mening for keys..
    // Her er constructeren for CustomerManager...
    // Til gengaeld

    Optional<OperationMirror> providingOperation();
}
// Root Container. RequiredServices = RuntimeResolvable?
// Giver jo mening at den kan fejle...

// - Satisfiable
// ! Nu proever vi at lave en version hvor alt er satisfiable... er kompliceret nok i forvejen
// Will fail with BuildException for any Resolution that is not satisfiable
//boolean isSatisfiable(); // isSatisfied?


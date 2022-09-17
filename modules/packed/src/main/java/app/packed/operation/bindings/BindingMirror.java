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
package app.packed.operation.bindings;

import java.lang.annotation.Annotation;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.container.UserOrExtension;
import app.packed.operation.OperationMirror;
import app.packed.operation.bindings.sandbox.ResolutionState;
import internal.app.packed.container.Mirror;

/**
 * A mirror for a dependency.
 */
// ; // What are we having injected... Giver det mening for functions????

// BiFunction(WebRequest, WebResponse) vs
// foo(WebRequest req, WebResponse res)
// Hvorfor ikke...
// Ja det giver mening!

// @WebRequst
// (HttpRequest, HttpResponse) == (r, p) -> ....

// Req + Response -> er jo operations variable...
// Tjah ikke

// Men er det dependencies??? Ja det er vel fx for @Provide
// Skal man kunne trace hvor de kommer fra??? Det vil jeg mene

// f.eks @Provide for et field ville ikke have dependencies

// Hvis den skal vaere extendable... saa fungere det sealed design ikke specielt godt?
// Eller maaske goer det? Taenker ikke man kan vaere alle dele
@SuppressWarnings("exports")
public sealed interface BindingMirror extends Mirror {

    ResolutionState resolutionState();

    boolean isSatisfiable(); //isSatisfied?

    /** {@return the operation that declares this dependency.} */
    OperationMirror operation();
    
    // We need a better name. It is the index into the list of dependencies for operation
    int operationIndex();
    
    Optional<UserOrExtension> providedBy(); // BeanMirror?

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

    // Tror det bliver ligesom OperationTarget
    non-sealed interface OfAnnotation extends BindingMirror {
        abstract Annotation annotation();

        abstract Class<? extends Annotation> annotationType();
    }

    non-sealed interface OfComposite extends BindingMirror {

        abstract BindingMirror bindings();

        // Tror ikke laengere vi bliver resolved som en compond.
        // get(Req, Res) -> Har bare 2 parametere. (Maaske idk)
        abstract boolean isFuncionalInterface();

    }

    non-sealed interface OfKey extends BindingMirror {
        abstract Key<?> key();
    }

    non-sealed interface OfOther extends BindingMirror {}

    non-sealed interface OfTyped extends BindingMirror {
        abstract Class<?> typed();
    }
}

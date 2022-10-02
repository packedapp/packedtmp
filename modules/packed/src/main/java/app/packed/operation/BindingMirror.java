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
package app.packed.operation;

import java.lang.annotation.Annotation;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import app.packed.container.UserOrExtension;
import app.packed.operation.bindings.BindingKind;
import app.packed.operation.bindings.DefaultMirror;
import app.packed.operation.bindings.ResolutionState;
import internal.app.packed.container.Mirror;
import internal.app.packed.operation.BindingSetup;

/**
 * A mirror representing the bound parameter of an operation.
 * 
 * @see OperationMirror#bindings()
 */
public class BindingMirror implements Mirror {

    /**
     * The internal configuration of the binding we are mirrored. Is initially null but populated via
     * {@link #initialize(BindingSetup)}.
     */
    @Nullable
    private BindingSetup binding;

    /** Create a new binding mirror. */
    public BindingMirror() {}

    /**
     * {@return the internal configuration of the binding.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(BindingSetup)} has not been called.
     */
    private BindingSetup binding() {
        BindingSetup b = binding;
        if (b == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return b;
    }

    /** {@return the index of this binding into OperationMirror#bindings().} */
    public int bindingIndex() { // alternative parameterIndex
        return binding().index;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof BindingMirror m && binding() == m.binding();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return binding().hashCode();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     * 
     * @param owner
     *            the internal configuration of the extension to mirror
     */
    final void initialize(BindingSetup binding) {
        if (this.binding != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.binding = binding;
    }

    /** {@return the operation that declares this binding.} */
    public OperationMirror operation() {
        return binding().operation.mirror();
    }

    /** {@return the underlying variable that has been bound.} */
    public Variable variable() {
        BindingSetup b = binding();
        return b.operation.type.parameter(b.index);
    }
}
//; // What are we having injected... Giver det mening for functions????

//BiFunction(WebRequest, WebResponse) vs
//foo(WebRequest req, WebResponse res)
//Hvorfor ikke...
//Ja det giver mening!

//@WebRequst
//(HttpRequest, HttpResponse) == (r, p) -> ....

//Req + Response -> er jo operations variable...
//Tjah ikke

//Men er det dependencies??? Ja det er vel fx for @Provide
//Skal man kunne trace hvor de kommer fra??? Det vil jeg mene

//f.eks @Provide for et field ville ikke have dependencies

//Hvis den skal vaere extendable... saa fungere det sealed design ikke specielt godt?
//Eller maaske goer det? Taenker ikke man kan vaere alle dele
interface Sandbox {
    BindingKind bindingKind();

    // Resolved
    // Unresolved + [Optional|Default]
    // RuntimeResolvable
    // Composite -> composite.all.isSatisfiable

    Optional<DefaultMirror> fallback(); // Do we parse it even if we have been build-time resolved????

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

    // Er det bare extension???

    // Hvad med manual bindings?

    // HttpRequst<?> Optional<Integer> er jo stadig provided af WebExtension...

    public UserOrExtension providedBy();

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

    public Optional<OperationMirror> providingOperation();

    public ResolutionState resolutionState();

    // Unresolved->Empty or Composite->Empty
    Optional<UserOrExtension> resolvedBy();

    Variable variableStripped(); // Remove @Nullable Quaifiers, Optional, PrimeAnnotation ect.. All annotations?? Maaske er det bare en type

    // Tror det bliver ligesom OperationTarget
    abstract class OfAnnotation extends BindingMirror {
        abstract Annotation annotation();

        abstract Class<? extends Annotation> annotationType();
    }

    public abstract class OfTyped extends BindingMirror {
        abstract Class<?> typed();
    }
}
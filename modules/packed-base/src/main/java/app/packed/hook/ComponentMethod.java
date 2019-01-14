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
package app.packed.hook;

import app.packed.container.Component;
import app.packed.container.Container;

/**
 * A component method is an immutable combination of a component, a component instance or mixin, and a single method on
 * the component instance. Instances of this class are normally acquired by calling
 * {@link Component#processMethods(java.util.function.Consumer) #methods()} or {@link Container#componentMethods()}.
 * <p>
 * This interface extends {@link Contextualizable} so that information can be stored per method. For example, you can
 * store information at startup time which needs to used when the component is shutdown. In this way you avoid needing
 * some kind of hash map to store these information.
 */
// ComponentInstanceMethodInvoker
// ComponentInvocable

// Vil man nogen gange bare have metoderne... som readonly, her taenker jeg paa analyse
// List<MethodDescriptor> components().methods()
// Det er lidt samme issue som med servicerne taenker jeg...

// Hov hvad med ComponentFields, is that a thing???............De kan kun have state you

public interface ComponentMethod /* extends Contextualizable, BindableMethod */ {

    /**
     * Returns a context map from this method.
     * <p>
     * Invoking this method is identical to invoking: {@code getComponent().attributes(getInstance(), getMethod())}
     **/
    // Skal paa en eller anden maade vaere saadan at metoder er unikke...
    // Ved ikke lige hvordan vi skal checke det....

    // @Override
    // Context context();

    // @Override
    // default <T> ComponentMethod bind(Class<T> type, T argument) {
    // BindableMethod.super.bind(type, argument);
    // return this;
    // }
    //
    // @Override
    // ComponentMethod bind(int index, Object argument);
    //
    // @Override
    // default <T> ComponentMethod bind(TypeLiteralOrKey<T> type, T argument) {
    // BindableMethod.super.bind(type, argument);
    // return this;
    // }
    //
    // @Override
    // default ComponentMethod clearBindings() {
    // BindableMethod.super.clearBindings();
    // return this;
    // }

    // If you want to disable error handling
    /**
     * Disables the standard error handling provided by
     *
     * @return this method
     */
    // TODO can't we just always add it???
    // default ComponentMethod disableErrorHandling() {
    // throw new UnsupportedOperationException();
    // }

    /**
     * Returns the component that manages the instance and method.
     *
     * @return the component that manages the instance and method
     */
    Component getComponent();

    /**
     * This method will use this method component's injector to bind every unbound parameter of this method.
     *
     * @return this method
     */
    // Paa en eller anden maade bliver vi noedt til at komme med en ordentlig fejlmeddellelse
    // Trying to execute method annotated with @OnStart. But
    default ComponentMethod inject() {
        // getComponent().injector().bind(this);
        return this;
    }

    default ComponentMethod injectOrFail() {
        // getComponent().injector().bind(this);
        // checkAllBound();
        return this;
    }

    // Er vel en slags AOP...
    // interruptOnShutdown();
    // interruptOnShutdown(long delay, TimeUnit unit);
}

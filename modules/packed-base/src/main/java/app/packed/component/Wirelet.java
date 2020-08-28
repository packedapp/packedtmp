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

import app.packed.container.ContainerBundle;
import app.packed.service.Injector;
import app.packed.service.InjectorAssembler;
import packed.internal.component.ComponentModifierSet;
import packed.internal.component.wirelet.InternalWirelet;
import packed.internal.component.wirelet.InternalWirelet.ComponentNameWirelet;
import packed.internal.component.wirelet.WireletList;

/**
 * Wirelets are an umbrella term for small pieces of glue code, that is used to wire together the components that make
 * up your application. connect, wire, instantiate, debug your applications.
 * 
 * A wiring operation is a piece of glue code that wire bundles and/or runtimes together, through operations such as
 * {@link InjectorAssembler#provideAll(Injector, Wirelet...)} or
 * <p>
 * As a rule of thumb wirelets are evaluated in order. For example, Wirelet.name("ffff"), Wirelet.name("sdsdsd"). Will
 * first the change the name to ffff, and then change it to sdsds. Maybe an example with.noStart + start_await it
 * better.
 * 
 * <p>
 * A typical usage for wiring operations is for rebinding services under another key when wiring an injector into
 * another injector.
 * 
 * start with peek, then import with peek around a service Available as {@code X} in one injector available under a key
 * {@code Y}
 * 
 * Show example where we rebind X to Y, and Y to X, maybe with a peek inbetween
 * 
 * Operations is order Example with rebind
 * <p>
 * Wirelet implementations must be immutable and safe to access by multiple concurrent threads. Unless otherwise
 * specified wirelets are reusable.
 * 
 */
// Maaske soerger containeren for at videre delegere extension wirelets...
// Saa man skal stadig haves Extension??? IDK
// Giver mere mening med at det skal vaere det intermediate element.
public abstract class Wirelet {

    final int modifiers;

    protected Wirelet() {
        modifiers = 0;
    }

    protected Wirelet(ComponentModifier modifier) {
        this.modifiers = ComponentModifierSet.setProperty(0, modifier);
    }

    /**
     * Combines an array or wirelets
     * 
     * @param wirelets
     *            the wirelets to combine
     * @return a combined {@code Wirelet}
     */
    public static Wirelet combine(Wirelet... wirelets) {
        return WireletList.of(wirelets);
    }

    /**
     * Returns a composed {@code Wirelet} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed operation.
     * If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param w1
     *            the operation to perform after this operation
     * 
     * @param w2
     *            the operation to perform after this operation
     * @return a combined {@code Wirelet} that performs in sequence this operation followed by the {@code after} operation
     */
    public static Wirelet combine(Wirelet w1, Wirelet w2) {
        return WireletList.of(w1, w2);
    }

    /**
     * Creates a wiring operation by composing a sequence of zero or more wiring operations.
     * 
     * @param wirelet
     *            stuff
     * @param others
     *            stuff
     * @return stuff
     */
    public static Wirelet combine(Wirelet wirelet, Wirelet[] others) {
        return WireletList.of(wirelet, others);
    }

    /**
     * Normally a wirelet must be handled. Meaning that the runtime, an extension or some user code must actually receive it
     * using {@link WireletHandler}. If this is not possible a runtime exception will be thrown when specifying the wirelet.
     * However, by wrapping the wire
     * 
     * @param wirelet
     *            the wirelet to wrap
     * @return a new wrapped wirelet
     */
    // Handled??? Unhandled (hmmm does not work VarHandle, MethodHandle)
    public static Wirelet ignoreUnhandled(Wirelet... wirelet) {
        return new InternalWirelet.IgnoreUnhandled(combine(wirelet));
    }

    public static boolean isAllAssignableTo(Class<? extends Wirelet> c, Wirelet... wirelets) {
        // Ideen er lidt at vi kan bruge den til at teste ting vi wrapper...
        // Eftersom folk kan smide dem i forskellige wrapper wirelets
        // such as combine and ignoreUnceceived
        if (wirelets.length == 0) {
            return true;
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that will set the name of the component to the specified name.
     * <p>
     * Overriding any default naming scheme, or any name that might already have been set, for example, via
     * {@link ContainerBundle#setName(String)}.
     * 
     * @param name
     *            the name of the component
     * @return a wirelet that can be used to set the name of the component
     */
    // String intrapolation?
    public static Wirelet named(String name) {
        return new ComponentNameWirelet(name);
    }

    // Skal vi tage en Component???
    // Eller kan vi kun validere med modifiers...
    protected final void validate() {

    }

    protected void unhandled() {

    }

//    /**
//     * @param wirelet
//     *            the wirelet to wrap
//     * @param property
//     *            the property that is required of the component
//     * @return the wrapped wirelet
//     */
//    // Det betyder at vi vel skal starte med kalkulere properties som noget af det foerste?
//    // Eller ogsaa at vi Wirelet skal vaere abstract
//    public static Wirelet requireModifier(Wirelet wirelet, ComponentModifier property) {
//        return wirelet;
//    }
}

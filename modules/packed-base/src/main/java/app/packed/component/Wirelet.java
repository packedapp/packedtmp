/*
v * Copyright (c) 2008 Kasper Nielsen.
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

import static java.util.Objects.requireNonNull;

import app.packed.container.ContainerWirelet;
import packed.internal.component.CompositeWirelet;
import packed.internal.component.InternalWirelet;
import packed.internal.component.InternalWirelet.OverrideNameWirelet;

/**
 * A wirelet is a small piece of "glue code" that can be specified when a component is wired.
 * <p>
 * Wirelets are typically used to debug foobar, sdsd.
 * 
 * , that is used to wire together the components that make up your program. connect, wire, instantiate, debug your
 * applications.
 * 
 * As a rule of thumb wirelets are evaluated in order. For example, Wirelet.name("ffff"), Wirelet.name("sdsdsd"). Will
 * first the change the name to ffff, and then change it to sdsds. Maybe an example with.noStart + start_await it
 * better.
 * <p>
 * You should never expose wirelet classes to the outside. As this
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
 * 
 * <p>
 * Wirelets are divided into 2 main types:
 * 
 * Packed Wirelet: These wirelets are defined by Packed itself can typically needs access to Packed's internal APIs.
 * 
 * Extension Wirelets: Wirelets that are defined by extensions and typically available via public static methods in a
 * XWirelet class.
 * 
 * There are 2 addition internal wirelet types which cannot be extended by users:
 * 
 * User Wirelets: Wirelets that are defined by end-users
 * 
 */
// Hvis vi kraever at alle SelectWirelets
// kun kan tage final eller/* sealed */wirelets som parameter
// Saa kan vi sikre os at ogsaa runtime wirelets bliver analyseret
// Og saa ville vi kunne bruge annoteringer...
// Men det fungere bare ikke godt hvis vi vil lave noget i constructor af en extension bean

public abstract sealed class Wirelet permits ContainerWirelet,BeanWirelet,InternalWirelet,CompositeWirelet {

    /**
     * Returns a combined wirelet that behaves, in sequence, as this wirelet followed by the {@code after} wirelet.
     * 
     * @param after
     *            the wirelet to process after this wirelet
     * @return the combined wirelet
     * @see #andThen(Wirelet...)
     * @see #beforeThis(Wirelet...)
     * @see #of(Wirelet...)
     */
    public final Wirelet andThen(Wirelet after) {
        requireNonNull(after, "after is null");
        return CompositeWirelet.of(this, after);
    }

    /**
     * Returns a combined wirelet that behaves, in sequence, as this wirelet followed by each of the specified wirelets.
     * <p>
     * If the specified array is empty, returns this wirelet.
     * 
     * @param wirelets
     *            the wirelets to process after this wirelet
     * @return the combined wirelet
     * @see #andThen(Wirelet)
     * @see #beforeThis(Wirelet...)
     * @see #of(Wirelet...)
     */
    public final Wirelet andThen(Wirelet... wirelets) {
        return CompositeWirelet.of(this, combine(wirelets));
    }

    /**
     * Returns a combined wirelet that behaves, in sequence, as each of the specified wirelets followed by this wirelet.
     * <p>
     * If the specified array is empty, returns this wirelet.
     * 
     * @param wirelets
     *            the wirelets to process before this wirelet
     * @return the combined wirelet
     * @see #andThen(Wirelet)
     * @see #beforeThis(Wirelet...)
     * @see #of(Wirelet...)
     */
    public final Wirelet beforeThis(Wirelet... wirelets) {
        return CompositeWirelet.of(combine(wirelets), this);
    }

    /** Attempting to wire a non-container component with this wirelet will fail. */
    protected static final void $requireContainer() {}

    /**
     * Attempting to wire a non-container component or a container component that is not the root with this wirelet will
     * fail.
     */
    protected static final void $requireContainerNonRoot() {}

    /** The wirelet can only be used on the root container in a namespace. */
    protected static final void $requireContainerRoot() {}

    protected static final void $stackable() {}

    // Altsaa den ville vaere god for MainArgsWirelet...
    // Folk maa gerne smide en MainArgsWirelet ind.
    // Vi kan nemlig ikke rigtig wrappe den.
    // Da det ikke er en statisk metode.

    /**
     * Combines multiple wirelets into a single wirelet that behaves, in sequence, as each of the specified wirelets.
     * 
     * @param wirelets
     *            the wirelets to combine
     * @return the combined wirelet
     * @see #andThen(Wirelet)
     * @see #andThen(Wirelet...)
     * @see #beforeThis(Wirelet...)
     */
    public static Wirelet combine(Wirelet... wirelets) {
        return CompositeWirelet.of(wirelets);
    }

    /**
     * Returns a wirelet that will set the name of the component to the specified name.
     * <p>
     * This wirelet override any name that might previously have been set, for example, via
     * {@link BaseBeanConfiguration#named(String)}.
     * 
     * @param name
     *            the name of the component
     * @return a wirelet that can be used to set the name of the component
     */
    // String intrapolation?
    public static Wirelet named(String name) {
        return new OverrideNameWirelet(name);
    }
}

///**
//* This
//* 
//* @param modifiers
//*/
//// Tror ikke vi bruger den her
//protected void unhandled(ComponentModifierSet modifiers) {
// // if package does not start with app.packed
// // Did you remember to annotated with @ExtensionMember
//}
//
//// Skal vi tage en Component???
//// Eller kan vi kun validere med modifiers...
//protected final void validate() {}

//
//static boolean isAllAssignableTo(Class<? extends Wirelet> c, Wirelet... wirelets) {
//  // Ideen er lidt at vi kan bruge den til at teste ting vi wrapper...
//  // Eftersom folk kan smide dem i forskellige wrapper wirelets
//  // such as combine and ignoreUnceceived
//  if (wirelets.length == 0) {
//      return true;
//  }
//  throw new UnsupportedOperationException();
//}
//public static Wirelet extractable(Wirelet wirelet) {
//throw new UnsupportedOperationException();
//}
//
///**
//* Normally a wirelet must be handled. Meaning that the runtime, an extension or some user code must actually consume it
//* using {@link WireletHandle}. If this is not possible a runtime exception will be thrown when specifying the wirelet.
//* However, by wrapping the wire
//* 
//* @param wirelet
//*            the wirelet to wrap
//* @return a new wrapped wirelet
//*/
//// Handled??? Unhandled (hmmm does not work VarHandle, MethodHandle)
//
//// Vi gider ikke have dem her.. fordi vi ikke gider have wirelets pakket ind i alt muligt...
//// Istedet skal alle informationer vaere statiske
//public static Wirelet ignoreUnhandled(Wirelet... wirelet) {
//  return new InternalWirelet.IgnoreUnhandled(combine(wirelet));
//}
//
//// will invoke the specified runnable if the wirelet cannot be processed
//// could be Wirelet.orElseRun(Runnable)...
//// orElseIgnore();
//// andThen()
//public static Wirelet ignoreUnhandled(Wirelet wirelet, Runnable orElseRun) {
//  return new InternalWirelet.IgnoreUnhandled(combine(wirelet));
//}

//protected ComponentSystemType scope() {
//  // Does not work with combine..
//  return ComponentSystemType.NAMESPACE;
//}

// Nej man har ikke saa mange luksuser som end-user taenker jeg???
// E
///**
//* @param wirelet
//*            the wirelet to wrap
//* @param property
//*            the property that is required of the component
//* @return the wrapped wirelet
//*/
//// Det betyder at vi vel skal starte med kalkulere properties som noget af det foerste?
//// Eller ogsaa at vi Wirelet skal vaere abstract
//public static Wirelet requireModifier(Wirelet wirelet, ComponentModifier property) {
//  return wirelet;
//}

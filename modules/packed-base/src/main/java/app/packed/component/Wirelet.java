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

import app.packed.container.Extension;
import packed.internal.component.wirelet.InternalWirelet;
import packed.internal.component.wirelet.InternalWirelet.SetComponentNameWirelet;
import packed.internal.component.wirelet.WireletList;
import packed.internal.component.wirelet.WireletPreModel;
import packed.internal.util.StackWalkerUtil;

/**
 * A wirelet is a small piece of "glue code" that can be specified when wiring a component. Wirelets are typically used
 * to debug foobar, sdsd.
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
 */
// Maaske soerger containeren for at videre delegere extension wirelets...
// Saa man skal stadig haves Extension??? IDK
// Giver mere mening med at det skal vaere det intermediate element.

// Hvis vi kraever at alle WireletHandle
// kun kan tage final eller/* sealed */wirelets som parameter
// Saa kan vi sikre os at ogsaa runtime wirelets bliver analyseret 
public abstract class Wirelet {

    /**
     * Returns a composed wirelet that performs, in sequence, this operation followed by the {@code after} operation. If
     * performing either operation throws an exception, it is relayed to the caller of the composed operation. If performing
     * this operation throws an exception, the {@code after} operation will not be performed.
     * 
     * @param wirelet
     *            the wirelet to process after this wirelet
     * @return the composed wirelet
     * @see #andThen(Wirelet)
     * @see #andThen(Wirelet...)
     * @see #beforeThis(Wirelet...)
     * @see #of(Wirelet...)
     */
    public final Wirelet andThen(Wirelet wirelet) {
        requireNonNull(wirelet, "wirelet is null");
        return WireletList.of(this, wirelet);
    }

    /**
     * 
     * @param wirelets
     *            the wirelets to process after this wirelet
     * @return the combined wirelet
     */
    public final Wirelet andThen(Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return this;
        }
        return WireletList.of(this, wirelets);
    }

    /**
     * @param wirelets
     *            wirelets
     * @return stuff
     * @see #andThen(Wirelet)
     * @see #andThen(Wirelet...)
     * @see #of(Wirelet...)
     */
    public final Wirelet beforeThis(Wirelet... wirelets) {
        return WireletList.of(wirelets, this);
    }

    /**
     * This
     * 
     * @param modifiers
     */
    protected void unhandled(ComponentModifierSet modifiers) {
        // if package does not start with app.packed
        // Did you remember to annotated with @ExtensionMember
    }

    // Skal vi tage en Component???
    // Eller kan vi kun validere med modifiers...
    protected final void validate() {}

    // cannot be consumed individually. Only as either
    // List or Set....
    // Must be a super type of this wirelet type
    // Is inherited
    // Can only be a part of one aggregate type...
    // And can only be injected as an aggregate type
    protected static final void $aggregateAs(Class<? extends Wirelet> wireletType) {
        WireletPreModel.stackBy(StackWalkerUtil.SW.getCallerClass(), wireletType);
    }

    /**
     * A static initializer method that indicates that the wirelet must be specified at build-time.
     * 
     * <p>
     * Wirelets cannot be specified at runtime. This prohibits the wirelet from being specified when using an image.
     * 
     * <p>
     * If this method is called from an inheritable wirelet. All subclasses of the wirelet will retain build-time only
     * status. Invoking this method on subclasses with a super class that have already invoked it. Will fail with an
     * exception(or error).
     * <p>
     * I think you can only have wirelets injected at build-time if they are build-time only... Nej, vi skal fx
     * bruge @Provide naar vi linker assemblies...
     */
    protected static final void $buildtimeOnly() {
        WireletPreModel.buildtimeOnly(StackWalkerUtil.SW.getCallerClass());
    }

    // Ideen er man ikke kan angives paa rod niveau
    //
    protected static final void $needsRealm() {
        // Wirelet.wireletRealm(Lookup); // <-- all subsequent wirelets
        // Wirelet.wireletRealm(Lookup, Wirelet... wirelets);

        // Tror det er vigtigt at der er forskel på REALM og BUILDTIME
        // Tror faktisk

        // f.x provide(Doo.class);
        // Hvad hvis vi koere composer.lookup()...
        // Saa laver vi jo saadan set en realm...
    }
    
    /** Attempting to wire a non-container component with this wirelet will fail. */
    protected static final void $requireContainer() {}

    /** Attempting to wire a non-container component or a container component that is not the root with this wirelet will fail. */
    protected static final void $requireContainerNonRoot() {}

    /** The wirelet can only be used on the root container in a namespace. */
    protected static final void $requireContainerRoot() {}

    // ExtensionWirelet... tror jeg...
    protected static final void $requireExtension(Class<? extends Extension> extensionClass) {
        // Will fail at runtime and at buildtime if extension is not installed...
        throw new UnsupportedOperationException();
    }

    // Altsaa den ville vaere god for MainArgsWirelet...
    // Folk maa gerne smide en MainArgsWirelet ind.
    // Vi kan nemlig ikke rigtig wrappe den.
    // Da det ikke er en statisk metode.

    public static Wirelet extractable(Wirelet wirelet) {
        throw new UnsupportedOperationException();
    }

    /**
     * Normally a wirelet must be handled. Meaning that the runtime, an extension or some user code must actually consume it
     * using {@link WireletHandle}. If this is not possible a runtime exception will be thrown when specifying the wirelet.
     * However, by wrapping the wire
     * 
     * @param wirelet
     *            the wirelet to wrap
     * @return a new wrapped wirelet
     */
    // Handled??? Unhandled (hmmm does not work VarHandle, MethodHandle)
    public static Wirelet ignoreUnhandled(Wirelet... wirelet) {
        return new InternalWirelet.IgnoreUnhandled(of(wirelet));
    }

    // will invoke the specified runnable if the wirelet cannot be processed
    // could be Wirelet.orElseRun(Runnable)...
    // orElseIgnore();
    // andThen()
    public static Wirelet ignoreUnhandled(Wirelet wirelet, Runnable orElseRun) {
        return new InternalWirelet.IgnoreUnhandled(of(wirelet));
    }

    static boolean isAllAssignableTo(Class<? extends Wirelet> c, Wirelet... wirelets) {
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
     * This wirelet override any name that might previously have been set, for example, via
     * {@link BaseComponentConfiguration#setName(String)}.
     * 
     * @param name
     *            the name of the component
     * @return a wirelet that can be used to set the name of the component
     */
    // String intrapolation?
    public static Wirelet named(String name) {
        return new SetComponentNameWirelet(name);
    }

    /**
     * Combines an array of wirelets into a single wirelet. Packed will automatically unpack any combined wirelets when
     * specified
     * 
     * @param wirelets
     *            the wirelets to combine
     * @return a combined {@code Wirelet}
     * @see #andThen(Wirelet)
     * @see #andThen(Wirelet...)
     * @see #beforeThis(Wirelet...)
     */
    public static Wirelet of(Wirelet... wirelets) {
        return WireletList.of(wirelets);
    }
}

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

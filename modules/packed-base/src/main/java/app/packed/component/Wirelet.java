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

import static java.util.Objects.requireNonNull;

import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import packed.internal.component.wirelet.BaseWirelet;
import packed.internal.component.wirelet.BaseWirelet.SetComponentNameWirelet;
import packed.internal.component.wirelet.WireletList;
import packed.internal.component.wirelet.WireletPreModel;

/**
 * Wirelets are an umbrella term for small pieces of glue code, that is used to wire together the components that make
 * up your program. connect, wire, instantiate, debug your applications.
 * 
 * A wiring operation is a piece of glue code that wire bundles and/or runtimes together, through operations such as
 * {@link ServiceExtension#provideAll(ServiceLocator)} or
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

    /** A stack walker used by various methods. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * 
     * @param wirelet
     *            the wirelet to process after this wirelet
     * @return the combined wirelet
     */
    public final Wirelet andThen(Wirelet wirelet) {
        requireNonNull(wirelet, "wirelet is null");
        return Wirelet.combine(this, wirelet);
    }

    /**
     * 
     * @param wirelets
     *            the wirelets to process after this wirelet
     * @return the combined wirelet
     */
    public final Wirelet andThen(Wirelet... wirelets) {
        return Wirelet.combine(this, wirelets);
    }

    // BeforeThis? PrecededBy
    public final Wirelet beforeThis(Wirelet wirelet) {
        return Wirelet.combine(wirelet, this);
    }

    public final Wirelet beforeThis(Wirelet... wirelets) {
        return Wirelet.combine(wirelets, this);
    }

//    protected ComponentSystemType scope() {
//        // Does not work with combine..
//        return ComponentSystemType.NAMESPACE;
//    }

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
        WireletPreModel.stackBy(STACK_WALKER.getCallerClass(), wireletType);
    }

    /**
     * A static initializer method that indicates that the wirelet must be specified at build-time.
     * 
     * <p>
     * Wirelets cannot be specified at runtime. This prohibits the wirelet from being specified when using an image.
     * 
     * <p>
     * If this method is called from an {@link InheritableWirelet}. All subclasses of the wirelet will retain build-time
     * only status. Invoking this method on subclasses with a super class that have already invoked it. Will fail with an
     * exception(or error).
     * <p>
     * I think you can only have wirelets injected at build-time if they are build-time only... Nej, vi skal fx
     * bruge @Provide naar vi linker assemblies...
     */
    protected static final void $buildtimeOnly() {
        WireletPreModel.buildtimeOnly(STACK_WALKER.getCallerClass());
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
    // syntes andThen()... before()

    /**
     * Creates a wiring operation by composing a sequence of zero or more wiring operations.
     * 
     * @param first
     *            stuff
     * @param last
     *            stuff
     * @return stuff
     */
    public static Wirelet combine(Wirelet first, Wirelet[] last) {
        return WireletList.of(first, last);
    }

    public static Wirelet combine(Wirelet[] first, Wirelet... last) {
        return WireletList.of(last, first);
    }

    public static Wirelet extractable(Wirelet wirelet) {
        throw new UnsupportedOperationException();
    }

    // Altsaa den ville vaere god for MainArgsWirelet...
    // Folk maa gerne smide en MainArgsWirelet ind.
    // Vi kan nemlig ikke rigtig wrappe den.
    // Da det ikke er en statisk metode.

    /**
     * Normally a wirelet must be handled. Meaning that the runtime, an extension or some user code must actually receive it
     * using {@link WireletReceive}. If this is not possible a runtime exception will be thrown when specifying the wirelet.
     * However, by wrapping the wire
     * 
     * @param wirelet
     *            the wirelet to wrap
     * @return a new wrapped wirelet
     */
    // Handled??? Unhandled (hmmm does not work VarHandle, MethodHandle)
    public static Wirelet ignoreUnhandled(Wirelet... wirelet) {
        return new BaseWirelet.IgnoreUnhandled(combine(wirelet));
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

    // will invoke the specified runnable if the wirelet cannot be processed
    // could be Wirelet.orElseRun(Runnable)...
    // orElseIgnore();
    // andThen()
    public static Wirelet ignoreUnhandled(Wirelet wirelet, Runnable orElseRun) {
        return new BaseWirelet.IgnoreUnhandled(combine(wirelet));
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
     * Overriding any default naming scheme, or any name that might already have been set, for example, via
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
}

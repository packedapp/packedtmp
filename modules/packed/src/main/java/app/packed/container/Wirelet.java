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
package app.packed.container;

import app.packed.extension.ExtensionWirelet;
import internal.app.packed.container.ContainerWirelets.ContainerOverrideNameWirelet;
import internal.app.packed.container.wirelets.CompositeWirelet;
import internal.app.packed.container.wirelets.FrameworkWirelet;

/**
 * Wirelets are a small pieces of "glue code" that can be specified when wiring containers at build time.
 * <p>
 * Wirelets are typically used to debug foobar, sdsd.
 *
 * <p>
 * Wirelets can
 *
 *
 * , that is used to wire together the components that make up your program. connect, wire, instantiate, debug your
 * applications.
 * <p>
 * As a rule of thumb wirelets are evaluated in the order they are specified. For example,
 * {@code Wirelet.named("foobar"), Wirelet.named("boofar")}. Will first the change the name to {@code "foobar"} and then
 * change it to {@code "boofar"}.
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
 * As a general rule, wirelet implementations must be immutable and safe to access by multiple concurrent threads.
 * Unless otherwise specified wirelets are reusable.
 * <p>
 *
 * Extension Wirelets: These wirelets are defined by extensions. These are typically available via public static methods
 * in a XWirelet class.
 *
 * Framework wirelets: These wirelets are defined by the framework. Cannot be extended by neither users or applications.
 *
 *
 * @see app.packed.assembly.AbstractBaseAssembly#selectWirelets(Class)
 * @see app.packed.extension.Extension#selectWirelets(Class)
 * @see ContainerConfiguration#selectWirelets(Class)
 */

// Important thinks.
// * Wirelets themselves are stateless and immutable
// * We need to able to specify where the wirelet can be used
// * We need to able to check if the wirelet is consumed at the site where it is used. It should be by default an error to not be consumed


// Wirelet specification sites

// Build : Application
// Build : BootstrapApp (prefix, postfix)

// Build : Link
// Build : ContainerTemplate  (prefix, postfix)

// Build : Assembly (prefix, postfix)  // Hmm, så kan vi sætte fx navn som postfix, så man ikke kan overskrive det???

// Build : protected List<Wirelet> Assembly.wirelets();  // Istedet for at wrappe den, eller begge dele? Nahh

// TrustedWirelet -> A wirelet that is defined within an assembly

public sealed abstract class Wirelet permits ExtensionWirelet, FrameworkWirelet {

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
     * @see #factoryOf(Wirelet...)
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
     * @see #factoryOf(Wirelet...)
     */
    public final Wirelet beforeThis(Wirelet... wirelets) {
        return CompositeWirelet.of(combine(wirelets), this);
    }

    /**
     * // * Attempting to wire a non-container component or a container component that is not the root with this wirelet
     * will // * fail. //
     */
//    protected static final void $requireContainerNonRoot() {}
//
//    /** The wirelet can only be used on the root container in a namespace. */
//    protected static final void $requireContainerRoot() {}

    // protected static final void $stackable() {}

    // Altsaa den ville vaere god for MainArgsWirelet...
    // Folk maa gerne smide en MainArgsWirelet ind.
    // Vi kan nemlig ikke rigtig wrappe den.
    // Da det ikke er en statisk metode.

    /**
     * Invoked by the runtime if the wirelet is not consumed. Either by {@link ContainerConfiguration#selectWirelets(Class)}
     * or {@link app.packed.extension.Extension#selectWirelets(Class)} or at runtime using injection of
     * {@link WireletSelection}.
     */
//    protected void onUnconsumed() {
//        // Invoked by the runtime if the wirelet is not processed in some way
//
//        // look up extension member
//        // HOW to know from this method whether or not the extension is in use???
//        // Could use ThreadLocal...
//        // Nej vi er ligeglade. En unprocessed extension wirelet...
//        // Er automatisk en extension der ikke er registreret
//        // Alle extensions skal processere alle deres wirelets
//
//        // Either the extension is not registered or some other
//
//        // Tror vi tester om den er overskrevet
//    }

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
    // Should probably have a similar rollout of wirelets.
    public static Wirelet combine(Wirelet... wirelets) {
        return CompositeWirelet.of(wirelets);
    }

//    // Nullable -> ignore
//    // Skal den evalueres paa build time eller runtime???
//    // Maaske 2 forskellige metoder
//    // Hvad er usecasen?
//    static Wirelet lazy(Supplier<@Nullable Wirelet> supplier) {
//        // We probably need a generic WrappingWirelet
//        throw new UnsupportedOperationException();
//    }

    /**
     * Returns a wirelet that will set the name of the application to the specified name.
     * <p>
     * This wirelet will have precedens over any name being set when building the application, for example, via
     * {@link ContainerConfiguration#named(String)}.
     * <p>
     *
     * @param name
     *            the name of the container
     * @return a wirelet that can be used to override the name of a container
     */
    // I think named() is fine
    public static Wirelet named(String name) {
        return new ContainerOverrideNameWirelet(name);
    }

}

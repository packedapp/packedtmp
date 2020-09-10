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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import app.packed.service.Injector;

/**
 * Shell drivers are responsible for creating new shells by instantiating stuff.
 * <p>
 * This class can be extended to create custom shell types if the built-in shell types such as {@link App} and
 * {@link Injector} are not sufficient. In fact, the default implementations of both {@link App} and {@link Injector}
 * are just thin facade that delegates all calls to an stuff instance.
 * <p>
 * Normally, you should never instantiate more then a single instance of driver for any shell implementation.
 * <p>
 * Iff a driver creates shells with an execution phase. The shell must implement {@link AutoCloseable}.
 * 
 * @param <S>
 *            The type of shell this driver creates.
 * @see App#driver()
 */

// Tror ikke shells kan bruge annoteringer??? Altsaa maaske paa surragates???
// Ville maaske vaere fedt nok bare at kunne sige
// @OnShutdown()
// sysout "FooBar was removed"

// Support of injection of the shell into the Container...
// We do not generally support this, as people are free to any shell they may like.
// Which would break encapsulation
public interface ShellDriver<S> {

    <C, D> S configure(ComponentDriver<D> driver, Function<D, C> factory, CustomConfigurator<C> consumer, Wirelet... wirelets);

    /**
     * Returns a set of the various modifiers that will by set on the underlying component. whether or not the type of shell
     * being created by this driver has an execution phase. This is determined by whether or not the shell implements
     * {@link AutoCloseable}.
     * 
     * @return whether or not the shell being produced by this driver has an execution phase
     */
    ComponentModifierSet modifiers();

    /**
     * Creates a new image using the specified bundle and this driver.
     * 
     * @param bundle
     *            the bundle to use when assembling the image
     * @param wirelets
     *            optional wirelets
     * @return a new image
     */
    Image<S> newImage(Bundle<?> bundle, Wirelet... wirelets);

    /**
     * Create a new shell (and its underlying system) using the specified bundle.
     * 
     * @param bundle
     *            the system bundle
     * @param wirelets
     *            optional wirelets
     * @return the new shell
     * @throws RuntimeException
     *             if the system could not be properly created
     */
    // Maaske kan vi laver en function der smider Throwable...
    S newShell(Bundle<?> bundle, Wirelet... wirelets);

    Class<?> rawType();

    static <A> ShellDriver<A> of(MethodHandles.Lookup caller, Class<A> shellType, Class<? extends A> implementation) {
        return PackedShellDriver.of(caller, shellType, implementation);
    }

    static <A> ShellDriver<A> of(MethodHandles.Lookup caller, Class<A> shellType, MethodHandle mh) {
        return PackedShellDriver.of(caller, shellType, mh);
    }
}

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

import app.packed.container.Container;
import app.packed.inject.ServiceLocator;
import packed.internal.classscan.InstantiatorBuilder;
import packed.internal.component.PackedInitializationContext;
import packed.internal.component.PackedShellDriver;
import packed.internal.inject.service.sandbox.Injector;

/**
 * Shell drivers are responsible for creating new shell instances, for example, instances of {@link App}.
 * <p>
 * This class can be extended to create custom shell types if the built-in shell types such as {@link App} and
 * {@link Injector} are not sufficient. In fact, the default implementations of both {@link App} and {@link Injector}
 * are just thin facade that delegates all calls to an stuff instance.
 * <p>
 * Normally, you would never create more than a single instance of a shell driver.
 * <p>
 * Iff a driver creates shells with an execution phase. The shell must implement {@link AutoCloseable}.
 * 
 * @param <S>
 *            The type of shell instances this driver create.
 * @see App#driver()
 * 
 * @apiNote In the future, if the Java language permits, {@link ShellDriver} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface ShellDriver<S> {

    <C extends Assembler, D> S configure(ComponentDriver<D> driver, Function<D, C> factory, CustomConfigurator<C> consumer, Wirelet... wirelets);

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
     * @throws BuildException
     *             if the system could not assembled properly
     */
    Image<S> newImage(Assembly<?> bundle, Wirelet... wirelets);

    /**
     * Create a new shell (and its underlying system) using the specified bundle.
     * 
     * @param bundle
     *            the system bundle
     * @param wirelets
     *            optional wirelets
     * @return the new shell
     * @throws BuildException
     *             if the system could not assembled properly
     */
    // Maaske kan vi laver en function der smider Throwable...
    S newShell(Assembly<?> bundle, Wirelet... wirelets);

    /**
     * Creates a new shell driver.
     * <p>
     * The specified implementation can have the following types injected.
     * 
     * If the specified implementation implements {@link AutoCloseable} a {@link Container} can also be injected.
     * <p>
     * Fields and methods are not processed.
     * 
     * @param <S>
     *            the type of shells the driver creates
     * @param caller
     *            a lookup object that must have full access to the specified implementation
     * @param implementation
     *            the implementation of the shell
     * @return a new driver
     */
    static <S> ShellDriver<S> of(MethodHandles.Lookup caller, Class<? extends S> implementation) {
        // We automatically assume that if the implementation implements AutoClosable. Then we need a guest.
        boolean isGuest = AutoCloseable.class.isAssignableFrom(implementation);

        // We currently do not support @Provide ect... Don't know if we ever will
        // Create a new MethodHandle that can create shell instances.

        InstantiatorBuilder ib = InstantiatorBuilder.of(caller, implementation, PackedInitializationContext.class);
        ib.addKey(Component.class, PackedInitializationContext.MH_COMPONENT, 0);
        ib.addKey(ServiceLocator.class, PackedInitializationContext.MH_SERVICES, 0);
        if (isGuest) {
            ib.addKey(Container.class, PackedInitializationContext.MH_GUEST, 0);
        }
        MethodHandle mh = ib.build();
        return new PackedShellDriver<>(isGuest, mh);
    }

    static <A> ShellDriver<A> of(MethodHandles.Lookup caller, Class<A> shellType, MethodHandle mh) {
        return PackedShellDriver.of(caller, shellType, mh);
    }
}
//Tror ikke shells kan bruge annoteringer??? Altsaa maaske paa surragates???
//Ville maaske vaere fedt nok bare at kunne sige
//@OnShutdown()
//sysout "FooBar was removed"

//Support of injection of the shell into the Container...
//We do not generally support this, as people are free to any shell they may like.
//Which would break encapsulation
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
package app.packed.application;

import java.lang.invoke.MethodHandles;

import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.operation.Op;
import app.packed.service.ServiceLocator;
import internal.app.packed.application.PackedApplicationDriver;

/**
 * A bootstrap app is a special application that can be used to creating other (non-bootstrap) application.
 * <p>
 * Bootstrap application Packed comes with a number of predefined application drivers:
 * <p>
 * Application drivers are normally never exposed to end users.
 * 
 * <p>
 * If these are not sufficient, it is very easy to build your own.
 * 
 * Which is probably your best bet is to look at the source code of them to create your own.
 * <p>
 * This class can be used to create custom artifact types if the built-in artifact types such as {@link App} and
 * {@link ServiceLocator} are not sufficient. In fact, the default implementations of both {@link App} and
 * {@link ServiceLocator} uses an artifact driver themselves.
 * <p>
 * Normally, you never create more than a single instance of an application driver.
 * 
 * @param <A>
 *            the type of applications this bootstrap app creates.
 */
@SuppressWarnings("rawtypes")
public sealed interface BootstrapApp<A> permits PackedApplicationDriver {

    /**
     * Builds an application using the specified assembly and optional wirelets and returns a new instance of it.
     * <p>
     * This method is typical not called directly by end-users. But indirectly through methods such as
     * {@link App#run(Assembly, Wirelet...)} .
     * 
     * @param assembly
     *            the main assembly of the application
     * @param wirelets
     *            optional wirelets
     * @return the launched application instance
     * @throws RuntimeException
     *             if the image could not be build
     * @throws LifecycleException
     *             if the application failed to initialize
     * @throws RuntimeException
     *             if the application had an executing phase and it fails
     * @see App#run(Assembly, Wirelet...)
     */
    A launch(Assembly assembly, Wirelet... wirelets); // newInstance

    ApplicationLauncher<A> newImage(Assembly assembly, Wirelet... wirelets);

    /**
     * Create a new application image by using the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly that should be used to build the image
     * @param wirelets
     *            optional wirelets
     * @return the new image
     * @throws RuntimeException
     *             if the image could not be build
     */
    // Andre image optimizations
    //// Don't cache beans info
    /// Nu bliver jeg i tvivl igen... Fx med Tester

    // launchLazily?
    ApplicationLauncher<A> newLauncher(Assembly assembly, Wirelet... wirelets);

    /**
     * Creates a new application mirror from the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly to create an application mirror from
     * @param wirelets
     *            optional wirelets
     * @return an application mirror
     * 
     * @throws RuntimeException
     *             if the mirror could not be build
     */
    ApplicationMirror newMirror(Assembly assembly, Wirelet... wirelets);

    /**
     * Verifies that a valid application can be build.
     * 
     * @param assembly
     *            the assembly defining the application that should be verified
     * @param wirelets
     *            optional wirelets
     * @throws RuntimeException
     *             if a valid application cannot be created
     */
    void verify(Assembly assembly, Wirelet... wirelets);

    /**
     * Augment the driver with the specified wirelets, that will be processed when building or instantiating new
     * applications.
     * <p>
     * For example, to : <pre> {@code
     * ApplicationDriver<App> driver = App.driver();
     * driver = driver.with(ApplicationWirelets.timeToRun(2, TimeUnit.MINUTES));
     * }</pre>
     * 
     * ApplicationW
     * <p>
     * This method will make no attempt of validating the specified wirelets.
     * 
     * <p>
     * Wirelets that were specified when creating the driver, or through previous invocation of this method. Will be
     * processed before the specified wirelets.
     * 
     * @param wirelets
     *            the wirelets to add
     * @return the augmented application driver
     */
    // Hvis vi ikke expo
    BootstrapApp<A> with(Wirelet... wirelets);

    static <A> BootstrapApp<A> of(Class<A> applicationClass, ComposerAction<Composer> action) {
        Composer c = new Composer();
        action.build(c);
        return c.b.build(MethodHandles.lookup(), applicationClass);
    }

    static BootstrapApp<Void> of(ComposerAction<Composer> action) {
        Composer c = new Composer();
        action.build(c);
        return c.b.buildVoid();
    }

    @SuppressWarnings("unchecked")
    static <A> BootstrapApp<A> of(Op<A> op, ComposerAction<Composer> action) {
        Composer c = new Composer();
        action.build(c);
        return c.b.build((Class) op.type().returnType(), op);
    }

    /**
     * A composer used to create bootstrap app instances.
     * 
     * @see BootstrapApp#of(Class, ComposerAction)
     * @see BootstrapApp#of(Op, ComposerAction)
     * @see BootstrapApp#of(ComposerAction)
     */
    // ? ApplicationWrapper
    // Bridge types
    // Compiler -> Deployable<ApplicationWrapper>
    public static final class Composer extends AbstractComposer {
        PackedApplicationDriver.Builder b = new PackedApplicationDriver.Builder(null);

        boolean managedLifetime;

        private Composer() {}

        /**
         * Application produced by the driver are executable. And will be launched by the specified launch mode by default.
         * <p>
         * The default launchState can be overridden at later point by using XYZ
         * 
         * @return this builder
         */
        public Composer managedLifetime() {
            b.managedLifetime();
            managedLifetime = true;
            return this;
        }

        static class BootstrapAppAssembly extends ComposerAssembly<Composer> {

            BootstrapAppAssembly(ComposerAction<? super Composer> action) {
                super(new Composer(), action);
            }
        }
    }
}

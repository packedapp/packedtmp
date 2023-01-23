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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import app.packed.application.BootstrapApp.Composer.BootstrapAppAssembly;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.framework.Nullable;
import app.packed.operation.Op;
import app.packed.service.ServiceLocator;
import internal.app.packed.application.ApplicationDriver;
import internal.app.packed.application.BootstrapAppSetup;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.lifetime.ApplicationInitializationContext;
import internal.app.packed.lifetime.sandbox.OldLifetimeKind;

/**
 * A bootstrap app is a special type of applications that can be used to create other (non-bootstrap) application.
 * <p>
 * Bootstrap application Packed comes with a number of predefined application drivers:
 * <p>
 * Bootstrap applications are normally never exposed to end users.
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
public final class BootstrapApp<A> {

    /** The internal bootstrap app. */
    private final BootstrapAppSetup<A> setup;

    BootstrapApp(BootstrapAppSetup<A> setup) {
        this.setup = requireNonNull(setup);
    }

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
    public A launch(Assembly assembly, Wirelet... wirelets) {
        return setup.launch(assembly, wirelets);
    }

    /**
     * Creates a new application mirror from the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly to create an application mirror from
     * @param wirelets
     *            optional wirelets
     * @return an application mirror
     * @throws RuntimeException
     *             if the mirror could not be created
     */
    public ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return setup.newMirror(assembly, wirelets);
    }

    public ApplicationLauncher<A> newImage(Assembly assembly, Wirelet... wirelets) {
        return setup.newImage(assembly, wirelets);
    }

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
    public ApplicationLauncher<A> newLauncher(Assembly assembly, Wirelet... wirelets) {
        return setup.newLauncher(assembly, wirelets);
    }

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
    public void verify(Assembly assembly, Wirelet... wirelets) {
        setup.verify(assembly, wirelets);
    }

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
    public BootstrapApp<A> with(Wirelet... wirelets) {
        return new BootstrapApp<>(setup.with(wirelets));
    }

    public static <A> BootstrapApp<A> of(Class<A> applicationClass, ComposerAction<Composer> action) {
        return of0(applicationClass, action);
    }

    public static BootstrapApp<Void> of(ComposerAction<? super Composer> action) {
        return of0(null, action);
    }

    public static <A> BootstrapApp<A> of(Op<A> op, ComposerAction<? super Composer> action) {
        return of0(op, action);
    }

    private static <A> BootstrapApp<A> of0(Object o, ComposerAction<? super Composer> action) {
        Composer comp = new Composer(o);
        PremordialApplicationDriver<A> pad = new PremordialApplicationDriver<>();
        BootstrapAppAssembly<Object> baa = new Composer.BootstrapAppAssembly<>(comp, action);
        AssemblySetup as = new AssemblySetup(pad, BuildGoal.LAUNCH, null, baa, new Wirelet[0]);
        as.build();

        MethodHandle mh = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationInitializationContext.class));
        if (o != null) {
            mh = comp.ahe.mh;
        }
        BootstrapAppSetup<A> a = new BootstrapAppSetup<>(comp.lifetimeKind, comp.mirrorSupplier, mh, null);
        return new BootstrapApp<>(a);
    }

    /**
     * A composer for creating bootstrap app instances.
     * 
     * @see BootstrapApp#of(Class, ComposerAction)
     * @see BootstrapApp#of(Op, ComposerAction)
     * @see BootstrapApp#of(ComposerAction)
     */
    // ? ApplicationWrapper
    // Bridge types
    // Compiler -> Deployable<ApplicationWrapper>
    public static final class Composer extends AbstractComposer {
        ApplicationHostExtension ahe;

        private OldLifetimeKind lifetimeKind = OldLifetimeKind.UNMANAGED;

        /** Supplies a mirror for the application. */
        private Supplier<? extends ApplicationMirror> mirrorSupplier = ApplicationMirror::new;

        final Object o;

        private Composer(Object o) {
            this.o = o;
        }

        /**
         * Application produced by the driver are executable. And will be launched by the specified launch mode by default.
         * <p>
         * The default launchState can be overridden at later point by using XYZ
         * 
         * @return this builder
         */
        public Composer managedLifetime() {
            this.lifetimeKind = OldLifetimeKind.MANAGED;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        protected void preCompose() {
            ahe = use(ApplicationHostExtension.class);
            if (o instanceof Class<?> cl) {
                ahe.newApplication(cl);
            } else if (o instanceof Op<?> op) {
                ahe.newApplication(op);
            }
        }

        public Composer specializeMirror(Supplier<? extends ApplicationMirror> mirrorSupplier) {
            this.mirrorSupplier = requireNonNull(mirrorSupplier, "mirrorSupplier is null");
            return this;
        }

        static class BootstrapAppAssembly<A> extends ComposerAssembly<Composer> {

            BootstrapAppAssembly(Composer c, ComposerAction<? super Composer> action) {
                super(c, action);
            }
        }
    }

    private static final class PremordialApplicationDriver<A> extends ApplicationDriver<BootstrapApp<A>> {

        /** {@inheritDoc} */
        @Override
        public OldLifetimeKind lifetimeKind() {
            return OldLifetimeKind.UNMANAGED;
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<? extends ApplicationMirror> mirrorSupplier() {
            return ApplicationMirror::new;
        }

        /** {@inheritDoc} */
        @Override
        public BootstrapApp<A> newInstance(ApplicationInitializationContext context) {
            throw new Error(); // We create the instance ourself
        }

        /** {@inheritDoc} */
        @Override
        @Nullable
        public Wirelet wirelet() {
            return null;
        }
    }
}

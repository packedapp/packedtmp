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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.application.BootstrapApp.Composer.BootstrapAppAssembly;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.extension.container.ExtensionLink;
import app.packed.lifetime.LifetimeKind;
import app.packed.operation.Op;
import app.packed.util.Nullable;
import internal.app.packed.application.AppSetup;
import internal.app.packed.application.AppSetup.ReusableApplicationImage;
import internal.app.packed.application.AppSetup.SingleShotApplicationImage;
import internal.app.packed.application.ApplicationDriver;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.RootApplicationBuilder;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.lifetime.PackedContainerLifetimeChannel;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;

/**
 * A bootstrap app is a special type of application that can be used to create other (non-bootstrap) application.
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
 * Normally, you never create more than a single instance of a bootstrap app.
 *
 * @param <A>
 *            the type of application this bootstrap app creates.
 */
public final /* primitive */ class BootstrapApp<A> {

    /** The internal bootstrap app. */
    private final AppSetup setup;

    private BootstrapApp(AppSetup setup) {
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
    @SuppressWarnings("unchecked")
    public A launch(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(setup, BuildGoal.LAUNCH_NOW);

        // Builds the application
        ApplicationSetup application = builder.build(assembly, wirelets);

        // Launch the application
        ApplicationInitializationContext aic = ApplicationInitializationContext.launch2(application, null);

        // Create an return an instance of the application interface
        return (A) setup.newInstance(aic);
    }

    /**
     * Builds an application and returns a mirror representing it.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return a mirror representing the application
     * @throws RuntimeException
     *             if the application could not be build
     */
    public ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(setup, BuildGoal.MIRROR);

        // Builds the application
        ApplicationSetup application = builder.build(assembly, wirelets);

        // Returns a mirror for the application
        return application.mirror();
    }

    public ApplicationLauncher<A> newImage(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(setup, BuildGoal.LAUNCH_REPEATABLE);

        // Builds the application
        ApplicationSetup application = builder.build(assembly, wirelets);

        // Create a reusable launcher
        return new ReusableApplicationImage<>(setup, application);
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
    // Andre image optimizations //// Don't cache beans info
    /// Nu bliver jeg i tvivl igen... Fx med Tester // launchLazily?
    public ApplicationLauncher<A> newLauncher(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(setup, BuildGoal.LAUNCH_LATER);

        // Builds the application
        ApplicationSetup application = builder.build(assembly, wirelets);

        // Creates a new single shot launcher
        return new SingleShotApplicationImage<>(setup, application);
    }

    /**
     * Builds and verifies an application.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @throws RuntimeException
     *             if the application could not be build
     */
    public void verify(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(setup, BuildGoal.VERIFY);

        // Builds (and verifies) the application
        builder.build(assembly, wirelets);
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
        PremordialApplicationDriver pad = new PremordialApplicationDriver();
        BootstrapAppAssembly<Object> baa = new Composer.BootstrapAppAssembly<>(comp, action);
        AssemblySetup as = new AssemblySetup(pad, BuildGoal.LAUNCH_NOW, null, baa, new Wirelet[0]);
        as.build();

        MethodHandle mh = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationInitializationContext.class));
        if (o != null) {
            mh = comp.ahe.mh;
        }
        mh = mh.asType(mh.type().changeReturnType(Object.class));

        AppSetup a = new AppSetup(comp.lifetimeKind, comp.mirrorSupplier, comp.channels, mh, null);
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

        /** Lifetime channels for the. */
        private final ArrayList<PackedContainerLifetimeChannel> channels = new ArrayList<>();

        private LifetimeKind lifetimeKind = LifetimeKind.UNMANAGED;

        /** Supplies a mirror for the application. */
        private Supplier<? extends ApplicationMirror> mirrorSupplier = ApplicationMirror::new;

        @Nullable
        final Object o;

        private Composer(@Nullable Object o) {
            this.o = o;
        }

        /**
         * Adds 1 or more container lifetime channels.
         *
         * @param channels
         *            the channel(s) to add
         * @return this composer
         */
        public Composer addChannel(ExtensionLink... channels) {
            this.channels.addAll(List.of(channels).stream().map(e -> (PackedContainerLifetimeChannel) e).toList());
            return this;
        }

        /**
         * Application produced by the driver are executable. And will be launched by the specified launch mode by default.
         * <p>
         * The default launchState can be overridden at later point by using XYZ
         *
         * @return this builder
         */
        public Composer managedLifetime() {
            this.lifetimeKind = LifetimeKind.MANAGED;
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

        /** An composer wrapping Assembly. */
        static class BootstrapAppAssembly<A> extends ComposerAssembly<Composer> {

            BootstrapAppAssembly(Composer c, ComposerAction<? super Composer> action) {
                super(c, action);
            }
        }
    }

    private static final class PremordialApplicationDriver extends ApplicationDriver {

        /** {@inheritDoc} */
        @Override
        public List<PackedContainerLifetimeChannel> channels() {
            return List.of();
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeKind lifetimeKind() {
            return LifetimeKind.UNMANAGED;
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<? extends ApplicationMirror> mirrorSupplier() {
            return ApplicationMirror::new;
        }

        /** {@inheritDoc} */
        @Override
        @Nullable
        public Wirelet wirelet() {
            return null;
        }
    }
}

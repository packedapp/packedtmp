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
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.BeanKind;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.extension.FrameworkExtension;
import app.packed.extension.bean.BeanBuilder;
import app.packed.extension.bean.BeanHandle;
import app.packed.extension.bean.BeanTemplate;
import app.packed.extension.container.ExtensionLink;
import app.packed.extension.context.ContextTemplate;
import app.packed.extension.operation.OperationTemplate;
import app.packed.lifetime.LifetimeKind;
import app.packed.operation.Op;
import app.packed.util.Nullable;
import internal.app.packed.container.AppSetup;
import internal.app.packed.container.ApplicationSetup;
import internal.app.packed.container.BootstrapAppBuilder;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.container.RootApplicationBuilder;
import internal.app.packed.container.AppSetup.ReusableApplicationImage;
import internal.app.packed.container.AppSetup.SingleShotApplicationImage;
import internal.app.packed.lifetime.PackedBeanTemplate;
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
 *
 * @see App
 * @see app.packed.extension.container.ContainerHolderService
 */
public final /* primitive */ class BootstrapApp<A> {

    /** The internal bootstrap app. */
    private final AppSetup setup;

    /**
     * Create a new bootstrap app
     *
     * @param setup
     *            the internal configuration of the app.
     */
    private BootstrapApp(AppSetup setup) {
        this.setup = requireNonNull(setup);
    }

    /**
     * Builds an application, launches it and returns the application instance.
     * <p>
     * Typically, methods calling this method is not named {@code launch} but instead something that better reflects what
     * exactly launch means for the particular type of application.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return the launched application instance
     * @throws RuntimeException
     *             if the application could not be built or failed to launch
     * @see App#run(Assembly, Wirelet...)
     */
    @SuppressWarnings("unchecked")
    public A launch(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(setup, BuildGoal.LAUNCH_NOW);

        // Builds the application
        ApplicationSetup application = builder.build(assembly, wirelets);

        // Launch the application
        ApplicationInitializationContext aic = ApplicationInitializationContext.launch(application, null);

        // Create an return an instance of the application interface
        return (A) setup.newInstance(aic);
    }

    /**
     * Builds an application and returns a mirror representing it.
     * <p>
     * If a special mirror supplied was set using {@link Composer#specializeMirror(Supplier)} when creating this bootstrap
     * app. The mirror returned from this method can be safely cast to the specialized application mirror.
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
     * BootstrapApp<App> app = ...;
     * app = app.with(ApplicationWirelets.timeToRun(2, TimeUnit.MINUTES));
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
     * @return the new bootstrap app
     */
    public BootstrapApp<A> with(Wirelet... wirelets) {
        return new BootstrapApp<>(setup.with(wirelets));
    }

    public static <A> BootstrapApp<A> of(Class<A> applicationClass, ComposerAction<Composer> action) {
        return of0(applicationClass, applicationClass, action);
    }

    public static BootstrapApp<Void> of(ComposerAction<? super Composer> action) {
        return of0(null, void.class, action);
    }

    public static <A> BootstrapApp<A> of(Op<A> op, ComposerAction<? super Composer> action) {
        return of0(op, op.type().returnRawType(), action);
    }

    private static <A> BootstrapApp<A> of0(@Nullable Object o, Class<?> type, ComposerAction<? super Composer> action) {
        Composer composer = new Composer(o, type);

        // Build the bootstrap application
        BootstrapAppBuilder builder = new BootstrapAppBuilder();
        builder.build(new Composer.BootstrapAppAssembly<>(composer, action));

        // Adapt the method handle
        MethodHandle mh = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationInitializationContext.class));
        if (o != null) {
            mh = composer.ahe.mh;
        }
        mh = mh.asType(mh.type().changeReturnType(Object.class));

        AppSetup a = new AppSetup(composer.lifetimeKind, composer.mirrorSupplier, composer.pct, mh, null);
        return new BootstrapApp<>(a);
    }

    private static class BootstrapAppExtension extends FrameworkExtension<BootstrapAppExtension> {

        static final ContextTemplate CIT = ContextTemplate.of(MethodHandles.lookup(), ApplicationInitializationContext.class,
                ApplicationInitializationContext.class);

        static final OperationTemplate ot = OperationTemplate.raw().withContext(CIT).returnTypeObject();

        static final BeanTemplate BLT = new PackedBeanTemplate(BeanKind.MANYTON).withOperationTemplate(ot);

        MethodHandle mh;

        private <T> void newApplication(BeanHandle<T> handle) {
            runOnCodegen(() -> mh = handle.lifetimeOperations().get(0).generateMethodHandle());
        }

        <T> void newApplication(Class<T> guestBean) {
            // We need the attachment, because ContainerGuest is on
            BeanBuilder bi = base().beanBuilder(BLT);
            newApplication(bi.install(guestBean));
        }

        <T> void newApplication(Op<T> guestBean) {
            // We need the attachment, because ContainerGuest is on
            BeanBuilder bi = base().beanBuilder(BLT);
            newApplication(bi.install(guestBean));
        }
    }

    /**
     * A composer for creating bootstrap app instances.
     *
     * @see BootstrapApp#of(Class, ComposerAction)
     * @see BootstrapApp#of(Op, ComposerAction)
     * @see BootstrapApp#of(ComposerAction)
     */
    public static final class Composer extends AbstractComposer {

        BootstrapAppExtension ahe;

        private LifetimeKind lifetimeKind = LifetimeKind.UNMANAGED;

        /** Supplies a mirror for the application. */
        private Supplier<? extends ApplicationMirror> mirrorSupplier = ApplicationMirror::new;

        @Nullable
        final Object o;

        PackedContainerTemplate pct;

        private Composer(@Nullable Object o, Class<?> type) {
            this.o = o;
            this.pct = new PackedContainerTemplate(PackedContainerKind.BOOTSTRAP, type, List.of());
        }

        /**
         * Adds 1 or more container lifetime channels.
         *
         * @param channels
         *            the channel(s) to add
         * @return this composer
         */
        public Composer addChannel(ExtensionLink... channels) {
            this.pct = (PackedContainerTemplate) pct.addLink(channels);
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
            ahe = use(BootstrapAppExtension.class);
            if (o instanceof Class<?> cl) {
                ahe.newApplication(cl);
            } else if (o instanceof Op<?> op) {
                ahe.newApplication(op);
            }
        }

        /**
         * Sets a special supplier that create application mirror instances
         *
         * @param mirrorSupplier
         *            an application mirror supplier
         * @return this composer
         *
         * @see BootstrapApp#mirrorOf(Assembly, Wirelet...)
         */
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
}

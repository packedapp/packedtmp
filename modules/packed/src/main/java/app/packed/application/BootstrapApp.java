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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanKind;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.extension.FrameworkExtension;
import app.packed.lifetime.LifetimeKind;
import app.packed.operation.Op;
import app.packed.util.Nullable;
import internal.app.packed.container.ApplicationSetup;
import internal.app.packed.container.CompositeWirelet;
import internal.app.packed.container.PackedContainerBuilder;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.lifetime.PackedBeanTemplate;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import internal.app.packed.util.ThrowableUtil;
import sandbox.extension.bean.BeanBuilder;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.bean.BeanTemplate;
import sandbox.extension.container.ContainerLifetimeTunnel;
import sandbox.extension.context.ContextTemplate;
import sandbox.extension.operation.OperationTemplate;

/**
 * A bootstrap app is a special type of application that can be used to create other (non-bootstrap) application. They
 * are typically not used directly by end-users. Instead end-users typically use wrappers such as {@link App} or
 * {@link app.packed.service.ServiceLocator}. These classes are typically thin wrappers around a bootstrap app instance.
 * <p>
 * If these are not sufficient, it is very easy to build your own.
 *
 * Which is probably your best bet is to look at the source code of them to create your own.
 * <p>
 * This class can be used to create custom artifact types if the built-in artifact types such as {@link App} and
 * {@link ServiceLocator} are not sufficient. In fact, the default implementations of both {@link App} and
 * {@link ServiceLocator} uses an artifact driver themselves.
 * <p>
 * One of the reasons packed is so cool is that. Is that packed uses an application to launch a new application.
 * <p>
 * Normally, you never create more than a single instance of a bootstrap app.
 *
 * @param <A>
 *            the type of application this bootstrap app creates.
 * @see App
 * @see app.packed.extension.container.ContainerHolderService
 */
public final /* primitive */ class BootstrapApp<A> {

    /** The internal bootstrap app. */
    private final BootstrapAppSetup setup;

    /**
     * Create a new bootstrap app
     *
     * @param setup
     *            the internal configuration of the app.
     */
    private BootstrapApp(BootstrapAppSetup setup) {
        this.setup = requireNonNull(setup);
    }

    public BootstrapApp<A> expectsResult(Class<?> resultType) {
        return this;
        // Ideen er bootstrapApp.expectsResult(FooBar.class).launch(...);
    }

    /**
     * Builds an application, launches it and returns an application interface instance (possible {@code void})
     * <p>
     * Typically, methods calling this method is not named {@code launch} but instead something that better reflects what
     * exactly launch means for the particular type of application.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return an application interface instance or void
     * @throws RuntimeException
     *             if the application could not be built or failed to launch
     * @see App#run(Assembly, Wirelet...)
     */
    @SuppressWarnings("unchecked")
    public A launch(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(setup, BuildGoal.LAUNCH_NOW);

        // Build the application
        ApplicationSetup application = builder.buildFromAssembly(assembly, wirelets).application;

        // Launch the application
        ApplicationLaunchContext aic = ApplicationLaunchContext.launch(application, null);

        // Create and return an instance of the application interface
        return (A) setup.newHolder(aic);
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

        // Build the application
        ApplicationSetup application = builder.buildFromAssembly(assembly, wirelets).application;

        // Returns a mirror for the application
        return application.mirror();
    }

    public ApplicationLauncher<A> newImage(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(setup, BuildGoal.LAUNCH_REPEATABLE);

        // Build the application
        ApplicationSetup application = builder.buildFromAssembly(assembly, wirelets).application;

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
        ApplicationSetup application = builder.buildFromAssembly(assembly, wirelets).application;

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
        builder.buildFromAssembly(assembly, wirelets);
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
        throw new UnsupportedOperationException(); // add a withTemplate method to BAAS
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

        // Create a new bootstrap app builder
        BootstrapAppBuilder builder = new BootstrapAppBuilder();

        // Builds the bootstrap application
        builder.buildFromAssembly(new Composer.BootstrapAppAssembly<>(composer, action));

        // Adapts the method handle
        MethodHandle mh;
        if (o == null) {
            // Produces null always. Expected signature BootstrapApp<Void>
            mh = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class));
        } else {
            mh = composer.ahe.mh;
            mh = mh.asType(mh.type().changeReturnType(Object.class));
        }

        BootstrapAppSetup a = new BootstrapAppSetup(composer.mirrorSupplier, composer.template, mh);
        return new BootstrapApp<>(a);
    }

    /** A container builder for creating {@link app.packed.application.BootstrapApp bootstrap applications}. */
    private static final class BootstrapAppBuilder extends PackedContainerBuilder {

        /** The container template used for {@link BootstrapApp}. */
        private static final PackedContainerTemplate TEMPLATE = new PackedContainerTemplate(PackedContainerKind.BOOTSTRAP_APPLICATION, BootstrapApp.class);

        /** Create new bootstrap builder with a fixed template. */
        public BootstrapAppBuilder() {
            super(TEMPLATE);
        }

        /** {@inheritDoc} */
        @Override
        public BuildGoal goal() {
            return BuildGoal.LAUNCH_NOW;
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeKind lifetimeKind() {
            return LifetimeKind.UNMANAGED;
        }
    }

    private static class BootstrapAppExtension extends FrameworkExtension<BootstrapAppExtension> {

        static final ContextTemplate CIT = ContextTemplate.of(MethodHandles.lookup(), ApplicationLaunchContext.class, ApplicationLaunchContext.class);

        static final OperationTemplate ot = OperationTemplate.raw().withContext(CIT).returnTypeObject();

        static final BeanTemplate ZBLT = new PackedBeanTemplate(BeanKind.MANYTON).withOperationTemplate(ot);

        MethodHandle mh;

        private <T> void newApplication(BeanHandle<T> handle) {
            runOnCodegen(() -> mh = handle.lifetimeOperations().get(0).generateMethodHandle());
        }

        <T> void newApplication(Class<T> guestBean) {
            // We need the attachment, because ContainerGuest is on
            BeanBuilder bi = base().beanBuilder(ZBLT);
            newApplication(bi.install(guestBean));
        }

        <T> void newApplication(Op<T> guestBean) {
            // We need the attachment, because ContainerGuest is on
            BeanBuilder bi = base().beanBuilder(ZBLT);
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

        /** Supplies a mirror for the application. */
        private Supplier<? extends ApplicationMirror> mirrorSupplier = ApplicationMirror::new;

        @Nullable
        final Object o;

        /** The template for the root container of the bootstrapped application. */
        private PackedContainerTemplate template;

        private Composer(@Nullable Object o, Class<?> type) {
            this.o = o;
            this.template = new PackedContainerTemplate(PackedContainerKind.ROOT_UNMANAGED, type);
        }

        /**
         * Adds 1 or more container lifetime channels.
         *
         * @param channels
         *            the channel(s) to add
         * @return this composer
         */
        public Composer addChannel(ContainerLifetimeTunnel... channels) {
            this.template = (PackedContainerTemplate) template.addLink(channels);
            return this;
        }

        public Composer expectsResult(Class<?> resultType) {
            this.template = template.expectResult(resultType);
            return this;
        }

        /**
         * Application produced by the driver are executable. And will be launched by the specified launch mode by default.
         *
         * @return this builder
         */
        // Sportsmaalet er hvad default er? Maaske unmanaged...
        public Composer managedLifetime() {
            this.template = template.withKind(PackedContainerKind.ROOT_MANAGED);
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

        // Flyt engang op. Saa getClass() er lidt mere printable?
        /** An composer wrapping Assembly. */
        private static class BootstrapAppAssembly<A> extends ComposerAssembly<Composer> {

            BootstrapAppAssembly(Composer c, ComposerAction<? super Composer> action) {
                super(c, action);
            }
        }
    }

    /** Used by {@link BootstrapApp} to build a single root application. */
    static final class RootApplicationBuilder extends PackedContainerBuilder {

        /** The build goal. */
        private final BuildGoal goal;

        RootApplicationBuilder(BootstrapAppSetup bootstrapApp, BuildGoal goal) {
            super(bootstrapApp.template());
            this.goal = goal;
            this.applicationMirrorSupplier = bootstrapApp.mirrorSupplier();
        }

        /** {@inheritDoc} */
        @Override
        public BuildGoal goal() {
            return goal;
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeKind lifetimeKind() {
            return LifetimeKind.UNMANAGED;
        }
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link BootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    static final class SingleShotApplicationImage<A> implements ApplicationLauncher<A> {

        private final AtomicReference<ReusableApplicationImage<A>> ref;

        SingleShotApplicationImage(BootstrapAppSetup driver, ApplicationSetup application) {
            this.ref = new AtomicReference<>(new ReusableApplicationImage<>(driver, application));
        }

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            ReusableApplicationImage<A> img = ref.getAndSet(null);
            if (img == null) {
                throw new IllegalStateException("This image has already been used");
            }
            // Not sure we can GC anything here
            // Think we need to extract a launcher and call it
            return img.launch(wirelets);
        }

    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link OldBootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    /* primitive */ record ReusableApplicationImage<A>(BootstrapAppSetup driver, ApplicationSetup application) implements ApplicationLauncher<A> {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public A launch(Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletWrapper wrapper = null;
            if (wirelets.length > 0) {
                wrapper = new WireletWrapper(CompositeWirelet.flattenAll(wirelets));
            }
            ApplicationLaunchContext aic = ApplicationLaunchContext.launch(application, wrapper);

            return (A) driver.newHolder(aic);
        }
    }

    /** The internal representation of a bootstrap app. */
    record BootstrapAppSetup(Supplier<? extends ApplicationMirror> mirrorSupplier, PackedContainerTemplate template, MethodHandle mh) {

        /**
         * Create a new application interface using the specified launch context.
         *
         * @param context
         *            the launch context to use for creating the application instance
         * @return the new application instance
         */
        public Object newHolder(ApplicationLaunchContext context) {
            try {
                return mh.invokeExact(context);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
    }

    /** A application launcher that maps the result of the launch. */
    /* primitive */ record MappedApplicationImage<A, F>(ApplicationLauncher<F> image, Function<? super F, ? extends A> mapper)
            implements ApplicationLauncher<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            F result = image.launch(wirelets);
            return mapper.apply(result);
        }

        /** {@inheritDoc} */
        @Override
        public <E> ApplicationLauncher<E> map(Function<? super A, ? extends E> mapper) {
            requireNonNull(mapper, "mapper is null");
            Function<? super F, ? extends E> andThen = this.mapper.andThen(mapper);
            return new MappedApplicationImage<>(image, andThen);
        }
    }
}

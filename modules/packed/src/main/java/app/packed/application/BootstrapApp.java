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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.application.BootstrapApp.Image;
import app.packed.bean.BeanKind;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.extension.ContainerLocal;
import app.packed.extension.FrameworkExtension;
import app.packed.lifetime.LifetimeKind;
import app.packed.lifetime.RunState;
import app.packed.operation.Op;
import app.packed.util.Result;
import internal.app.packed.container.ApplicationSetup;
import internal.app.packed.container.CompositeWirelet;
import internal.app.packed.container.FutureApplicationSetup;
import internal.app.packed.container.NonBootstrapContainerBuilder;
import internal.app.packed.container.PackedContainerBuilder;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.container.WireletSelectionArray;
import internal.app.packed.context.publish.ContextTemplate;
import internal.app.packed.lifetime.PackedBeanTemplate;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import internal.app.packed.util.ThrowableUtil;
import sandbox.extension.bean.BeanBuilder;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.bean.BeanTemplate;
import sandbox.extension.container.ContainerLifetimeTunnel;
import sandbox.extension.operation.OperationTemplate;

/**
 * A bootstrap app is a special type of application that can be used to create other (non-bootstrap) application.
 * <p>
 * Bootstrap applications are rarely used directly by end-users. Instead end-users typically use thin wrappers such as
 * {@link App} or {@link app.packed.service.ServiceLocator} to create new applications. However, if greater control of
 * the application is needed end-users may create their own bootstrap application.
 * <p>
 * Normally, you never create more than a single instance of a bootstrap app. Bootstrap applications are in general
 * stateless and safe to use concurrently.
 *
 * @param <A>
 *            the type of application this bootstrap app creates.
 * @see App
 * @see JobApp
 * @see DaemonApp
 * @see app.packed.cli.CliApp
 * @see app.packed.service.ServiceLocator
 */
public final /* primitive */ class BootstrapApp<A> {

    /** The internal bootstrap app. */
    private final Holder holder;

    /**
     * Create a new bootstrap app
     *
     * @param setup
     *            the internal configuration of the app.
     */
    private BootstrapApp(Holder holder) {
        this.holder = requireNonNull(holder);
    }

    public BootstrapApp<A> expectsResult(Class<?> resultType) {
        return this;
        // Ideen er bootstrapApp.expectsResult(FooBar.class).launch(...);
    }

    /**
     * An application image is a stand-alone program, derived from an {@link app.packed.container.Assembly}, which runs the
     * application represented by the assembly — and no other.
     * <p>
     * By configuring an image ahead of time, the actual time to instantiation the system can be severely decreased often
     * down to a couple of microseconds. In addition to this, images can be reusable, so you can create multiple systems
     * from a single image.
     * <p>
     * Application images typically have two main use cases:
     *
     * GraalVM Native Image
     *
     * Recurrent instantiation of the same application.
     *
     * Creating artifacts in Packed is already really fast, and you can easily create one 10 or hundres of microseconds. But
     * by using artifact images you can into hundres or thousounds of nanoseconds.
     * <p>
     * Use cases: Extremely fast startup.. graal
     *
     * Instantiate the same container many times
     * <p>
     * Limitations:
     *
     * No structural changes... Only whole artifacts
     *
     * <p>
     * An image can be used to create new instances of {@link app.packed.application.App} or other applications. Artifact
     * images can not be used as a part of other containers, for example, via
     *
     * @see App#imageOf(Assembly, Wirelet...)
     */
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
    public Image<A> imageOf(Assembly assembly, Wirelet... wirelets) {
        RootApplicationBuilder builder = new RootApplicationBuilder(holder, BuildGoal.IMAGE);

        // Assign to Assembly
        builder.processBuildWirelet(wirelets);

        Image<A> image;

        if (builder.optionBuildApplicationLazy) {
            FutureApplicationSetup fas = new FutureApplicationSetup(builder, assembly);
            image = new LazyApplicationImage<>(holder, fas);
        } else {
            ApplicationSetup application = builder.buildNow(assembly).application;
            image = new EagerApplicationImage<>(holder, application);
        }

        if (!builder.optionBuildReusableImage) {
            image = new NonReusableApplicationImage<>(image);
        }
        return image;
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
        RootApplicationBuilder builder = new RootApplicationBuilder(holder, BuildGoal.LAUNCH);

        builder.processBuildWirelet(wirelets);

        // Build the application
        ApplicationSetup application = builder.buildNow(assembly).application;

        // Launch the application
        ApplicationLaunchContext aic = ApplicationLaunchContext.launch(application, null);

        // Create and return an instance of the application interface
        return (A) holder.newHolder(aic);
    }

// Eller maasske er den simpelthen en nested class (Image) paa BootstrapApp???

// If general used

    // Hvorfor ikke bruge BootstrapApp'en som en launcher???
    // Det betyder selv at vi altid skal chaine..
    // Men det er vel ok
    // map()->
    public Launcher<A> launcher() {
        throw new UnsupportedOperationException();
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
        RootApplicationBuilder builder = new RootApplicationBuilder(holder, BuildGoal.MIRROR);

        builder.processBuildWirelet(wirelets);

        // Build the application
        ApplicationSetup application = builder.buildNow(assembly).application;

        // Returns a mirror for the application
        return application.mirror();
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
        RootApplicationBuilder builder = new RootApplicationBuilder(holder, BuildGoal.VERIFY);

        builder.processBuildWirelet(wirelets);

        // Builds (and verifies) the application
        builder.buildNow(assembly);
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
        return new BootstrapApp<>(new Holder(holder.mirrorSupplier, holder.template.withWirelets(wirelets), holder.mh));
    }

    public <E> BootstrapApp<E> map(Function<? super A, ? extends E> mapper) {
        throw new UnsupportedOperationException();
    }

    public static <A> BootstrapApp<A> of(Class<A> applicationClass, ComposerAction<? super Composer> action) {
        return of0(applicationClass, applicationClass, action);
    }

    public static BootstrapApp<Void> of(ComposerAction<? super Composer> action) {
        return of(Void.class, action);
    }

    public static <A> BootstrapApp<A> of(Op<A> op, ComposerAction<? super Composer> action) {
        return of0(op, op.type().returnRawType(), action);
    }

    private static <A> BootstrapApp<A> of0(Object opOrClass, Class<?> type, ComposerAction<? super Composer> action) {
        Composer composer = new Composer(opOrClass, type);

        // Create a new bootstrap app builder
        BootstrapAppBuilder builder = new BootstrapAppBuilder();

        // Builds the bootstrap application
        builder.buildNow(new Composer.BootstrapAppAssembly(composer, action));

        // Adapts the method handle
        MethodHandle mh;
        if (opOrClass == Void.class) {
            // Produces null always. Expected signature BootstrapApp<Void>
            mh = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class));
        } else {
            mh = composer.bootstrapExtension.mh;
            mh = mh.asType(mh.type().changeReturnType(Object.class));
        }

        Holder a = new Holder(composer.mirrorSupplier, composer.template, mh);
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
            return BuildGoal.LAUNCH;
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

        static final BeanTemplate BT = new PackedBeanTemplate(BeanKind.MANYTON).withOperationTemplate(ot);

        MethodHandle mh;

        private <T> void newApplication(BeanHandle<T> handle) {
            runOnCodegen(() -> mh = handle.lifetimeOperations().get(0).generateMethodHandle());
        }

        <T> void newApplication(Class<T> guestBean) {
            // We need the attachment, because ContainerGuest is on
            BeanBuilder bi = base().beanBuilder(BT);
            newApplication(bi.install(guestBean));
        }

        <T> void newApplication(Op<T> guestBean) {
            // We need the attachment, because ContainerGuest is on
            BeanBuilder bi = base().beanBuilder(BT);
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

        private BootstrapAppExtension bootstrapExtension;

        /** Supplies a mirror for the application. */
        private Supplier<? extends ApplicationMirror> mirrorSupplier = ApplicationMirror::new;

        private final Object opOrClass;

        /** The template for the root container of the bootstrapped application. */
        private PackedContainerTemplate template;

        private Composer(Object opOrClass, Class<?> type) {
            this.opOrClass = opOrClass;
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
            bootstrapExtension = use(BootstrapAppExtension.class);
            if (opOrClass == Void.class) {
                // ignore
            } else if (opOrClass instanceof Class<?> cl) {
                bootstrapExtension.newApplication(cl);
            } else if (opOrClass instanceof Op<?> op) {
                bootstrapExtension.newApplication(op);
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

        public Composer wirelets(Wirelet... wirelets) {
            this.template = template.withWirelets(wirelets);
            return this;
        }

        /** An composer wrapping Assembly. */
        private static class BootstrapAppAssembly extends ComposableAssembly<Composer> {

            private BootstrapAppAssembly(Composer c, ComposerAction<? super Composer> action) {
                super(c, action);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public sealed interface Image<A> permits NonReusableApplicationImage, EagerApplicationImage, LazyApplicationImage, MappedApplicationImage {

        // BootstrapResult
        default Result<A> compute(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        /**
         * Launches an instance of the application that this image represents.
         * <p>
         * Launches an instance of the application. What happens here is dependent on application driver that created the image.
         * The behaviour of this method is identical to {@link BootstrapApp#launch(Assembly, Wirelet...)}.
         *
         * @param wirelets
         *            optional wirelets
         * @return an application instance
         *
         * @see BootstrapApp#launch(Assembly, Wirelet...)
         */
        A launch(Wirelet... wirelets);

        /**
         * Returns a new application image that maps the result of the launch.
         *
         * @param <E>
         *            the type to map the launch result to
         * @param mapper
         *            the mapper
         * @return a new application image that maps the result of the launch
         */
        default <E> Image<E> map(Function<? super A, ? extends E> mapper) {
            requireNonNull(mapper, "mapper is null");
            return new MappedApplicationImage<>(this, mapper);
        }
    }

    /**
     * A launcher is used before an application is launched or an image is created.
     */
    public interface Launcher<A> {

        Image<A> imageOf(Assembly assembly, Wirelet... wirelets);

        A launch(Assembly assembly, Wirelet... wirelets);

        /**
         * Returns a new launcher that maps the result of the launch.
         *
         * @param <E>
         *            the type to map the launch result to
         * @param mapper
         *            the mapper
         * @return a new application image that maps the result of the launch
         */
        <E> Launcher<E> map(Function<? super A, ? extends E> mapper);

        ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets);

        <T> Launcher<A> setLocal(ContainerLocal<T> local, T value);

        void verify(Assembly assembly, Wirelet... wirelets);
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link BootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    static final class NonReusableApplicationImage<A> implements Image<A> {

        /** An atomic reference to an application image. Is used once */
        private final AtomicReference<Image<A>> ref;

        NonReusableApplicationImage(Holder driver, ApplicationSetup application) {
            this.ref = new AtomicReference<>(new EagerApplicationImage<>(driver, application));
        }

        NonReusableApplicationImage(Image<A> image) {
            this.ref = new AtomicReference<>(image);
        }

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            Image<A> img = ref.getAndSet(null);
            if (img == null) {
                throw new IllegalStateException(
                        "This image has already been used. You can use ApplicationWirelets.resuableImage() to allow repeatable usage of an application image");
            }
            // Not sure we can GC anything here
            // Think we need to extract a launcher and call it
            return img.launch(wirelets);
        }
    }

    /** Used by {@link BootstrapApp} to build a single root application. */
    private static final class RootApplicationBuilder extends NonBootstrapContainerBuilder {

        /** The build goal. */
        private final BuildGoal goal;

        // shutdown hooks

        RootApplicationBuilder(Holder bootstrapApp, BuildGoal goal) {
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
     * Implementation of {@link ApplicationLauncher} used by {@link OldBootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    /* primitive */ record EagerApplicationImage<A>(Holder driver, ApplicationSetup application) implements Image<A> {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public A launch(Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletSelectionArray<?> wrapper = null;
            if (wirelets.length > 0) {
                wrapper = WireletSelectionArray.of(CompositeWirelet.flattenAll(wirelets));
            }
            ApplicationLaunchContext aic = ApplicationLaunchContext.launch(application, wrapper);

            return (A) driver.newHolder(aic);
        }
    }

    /* primitive */ record LazyApplicationImage<A>(Holder holder, FutureApplicationSetup application) implements Image<A> {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public A launch(Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletSelectionArray<?> wrapper = null;
            if (wirelets.length > 0) {
                wrapper = WireletSelectionArray.of(CompositeWirelet.flattenAll(wirelets));
            }
            ApplicationLaunchContext aic = ApplicationLaunchContext.launch(application.lazyBuild(), wrapper);

            return (A) holder.newHolder(aic);
        }
    }

    /** The internal configuration of a bootstrap app. */
    private record Holder(Supplier<? extends ApplicationMirror> mirrorSupplier, PackedContainerTemplate template, MethodHandle mh) {

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

    /** A application launcher that maps the result of launching. */
    /* primitive */ record MappedApplicationImage<A, F>(Image<F> image, Function<? super F, ? extends A> mapper) implements Image<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            F result = image.launch(wirelets);
            return mapper.apply(result);
        }

        /** {@inheritDoc} */
        @Override
        public <E> Image<E> map(Function<? super A, ? extends E> mapper) {
            requireNonNull(mapper, "mapper is null");
            Function<? super F, ? extends E> andThen = this.mapper.andThen(mapper);
            return new MappedApplicationImage<>(image, andThen);
        }
    }
}

interface Zimgbox<A> {

//  /**
//  * Launches an instance of the application that this image represents.
//  *
//  * @throws ApplicationLaunchException
//  *             if the application failed to launch
//  * @throws IllegalStateException
//  *             if the image has already been used to launch an application and the image is not a reusable image
//  * @return the application interface if available
//  */
// default A checkedLaunch() throws ApplicationLaunchException {
//     return checkedLaunch(new Wirelet[] {});
// }
//
// default A checkedLaunch(Wirelet... wirelets) throws ApplicationLaunchException {
//     throw new UnsupportedOperationException();
// }

    default boolean isUseable() {
        // An image returns true always

        // Optional<A> tryLaunch(Wirelet... wirelets)???
        return true;
    }

    /**
     * Returns the launch mode of application(s) created by this image.
     *
     * @return the launch mode of the application
     *
     */
    RunState launchMode(); // usageMode??

    Optional<ApplicationMirror> mirror();

    // Hmmmmmmm IDK
    // Could do sneaky throws instead
    A throwingUse(Wirelet... wirelets) throws Throwable;

    default Image<A> with(Wirelet... wirelets) {
        // Egentlig er den kun her pga Launcher
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a mirror for the application if available.
     *
     * @param image
     *            the image to extract the application mirror from
     * @return a mirror for the application
     * @throws UnsupportedOperationException
     *             if the specified image was not build with BuildWirelets.retainApplicationMirror()
     */
    // Eller bare Optional<Mirror>
    static ApplicationMirror extractMirror(Image<?> image) {
        throw new UnsupportedOperationException();
    }

    // ALWAYS HAS A CAUSE
    // Problemet jeg ser er, hvad skal launch smide? UndeclaredThrowableException

    // App.execute
    // App.checkedExecute <---

    // Maaske er det LifetimeLaunchException
//    public static class ApplicationLaunchException extends Exception {
//
//        private static final long serialVersionUID = 1L;
//
//        RunState state() {
//            return RunState.INITIALIZED;
//        }
//    }
}

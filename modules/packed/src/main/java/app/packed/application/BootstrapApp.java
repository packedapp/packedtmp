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
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.application.ApplicationTemplate.Installer;
import app.packed.assembly.AbstractComposer;
import app.packed.assembly.AbstractComposer.ComposerAction;
import app.packed.assembly.Assembly;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.extension.FrameworkExtension;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import app.packed.runtime.RunState;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.container.PackedContainerTemplate.PackedContainerTemplateConfigurator;
import internal.app.packed.context.publish.ContextTemplate;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import sandbox.extension.container.ContainerTemplateLink;

/**
 * A bootstrap app is a special type of application that can be used to create other (non-bootstrap) application.
 * <p>
 * Bootstrap apps cannot directly modify the applications that it bootstraps. It cannot, for example, install an
 * extension in the application. However, it can say it can only bootstrap applications that have the extension
 * installed, failing with a build exception if the developer does not install the extension. As such, the bootstrap app
 * can only setup requirements for the application that it bootstraps. It cannot directly make the needed changes to the
 * bootstrapped application.
 * <p>
 * Bootstrap applications are rarely used directly by users. Instead users typically use thin wrappers such as
 * {@link App} or {@link app.packed.service.ServiceLocator} to create new applications. However, if greater control of
 * the application is needed users may create their own bootstrap application.
 * <p>
 * Normally, you never create more than a single instance of a bootstrap app. Bootstrap applications are, unless
 * otherwise specified, safe to use concurrently.
 *
 * @param <A>
 *            the type of application this bootstrap app creates.
 * @see App
 * @see JobApp
 * @see DaemonApp
 * @see app.packed.cli.CliApp
 * @see app.packed.service.ServiceLocator
 */
public final /* value */ class BootstrapApp<A> {

    /** The application template for new applications. */
    private final PackedApplicationTemplate template;

    /**
     * Create a new bootstrap app
     *
     * @param template
     *            the template for the apps that are being bootstrapped.
     */
    private BootstrapApp(PackedApplicationTemplate template) {
        this.template = requireNonNull(template);
    }

    public BootstrapApp<A> expectsResult(Class<?> resultType) {
        // Ideen er bootstrapApp.expectsResult(FooBar.class).launch(...);
        return this;
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
     * Create a new base image by using the specified assembly and optional wirelets.
     *
     * @param assembly
     *            the assembly that should be used to build the image
     * @param wirelets
     *            optional wirelets
     * @return the new base image
     * @throws RuntimeException
     *             if the image could not be build
     */
    @SuppressWarnings("unchecked")
    public BaseImage<A> imageOf(Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer installer = template.newInstaller(BuildGoal.IMAGE, wirelets);

        // Build the application
        ApplicationHandle<?, ?> handle = installer.install(assembly, BootstrapApplicationHandle::new);

        // Returns an image for the application
        return (BaseImage<A>) handle.image();
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
        ApplicationTemplate.Installer installer = template.newInstaller(BuildGoal.LAUNCH, wirelets);

        // Build the application
        ApplicationHandle<?, ?> handle = installer.install(assembly, BootstrapApplicationHandle::new);

        // Launch the application
        ApplicationLaunchContext aic = ApplicationLaunchContext.launch(handle, null);

        // Create and return an instance of the application interface
        return (A) template.newHolder(aic);
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
        ApplicationTemplate.Installer installer = template.newInstaller(BuildGoal.MIRROR, wirelets);

        // Build the application
        ApplicationHandle<?, ?> handle = installer.install(assembly, BootstrapApplicationHandle::new);

        // Returns a mirror for the application
        return handle.mirror();
    }

    /**
     * Builds and verifies an application.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @throws RuntimeException
     *             if the application could not be build or verified
     */
    public void verify(Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer installer = template.newInstaller(BuildGoal.VERIFY, wirelets);

        // Builds (and verifies) the application
        installer.install(assembly, BootstrapApplicationHandle::new);
    }

    /**
     * Create a new bootstrap application for application of the specified type
     *
     * @param <A>
     * @param applicationClass
     * @param action
     * @return
     */
    public static <A> BootstrapApp<A> of(Class<A> hostClass, ComposerAction<? super Composer> action) {
        return of0(hostClass, hostClass, action);
    }

    public static BootstrapApp<Void> of(ApplicationTemplate template, ComposerAction<? super Composer> action) {
        return of(Void.class, action);
    }

    public static <A> BootstrapApp<A> of(Op<A> hostOp, ComposerAction<? super Composer> action) {
        return of0(hostOp, hostOp.type().returnRawType(), action);
    }

    private static <A> BootstrapApp<A> of0(Object opOrClass, Class<?> type, ComposerAction<? super Composer> action) {
        Composer composer = new Composer(opOrClass, type);

        // Create new installer for the bootstrap app
        PackedApplicationInstaller installer = PackedApplicationTemplate.newBootstrapAppInstaller();

        // Builds the bootstrap application
        installer.buildApplication(new Composer.BootstrapAppAssembly(composer, action));

        // Adapts the method handle
        MethodHandle mh;
        if (opOrClass == Void.class) {
            // Produces null always. Expected signature BootstrapApp<Void>
            mh = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class));
        } else {
            mh = composer.bootstrapExtension.mh;
            mh = mh.asType(mh.type().changeReturnType(Object.class));
        }

        PackedApplicationTemplate t = new PackedApplicationTemplate(composer.template.pbt, mh);
        return new BootstrapApp<>(t);
    }

    // Enten kan vi extender ApplicationConfigurator paa Composeren..
    // Altsaa vi skal bruge en Assembly... til at lave bootstrap app'en i modsaetning til en normal ApplicationTemplate
    public static <A> BootstrapApp<A> of2(Class<A> hostClass, Consumer<? super ApplicationTemplate.Configurator> action) {
        throw new UnsupportedOperationException();
    }

    /** A special kind of extension that is used to build a bootstrap application. */
    private static class BootstrapAppExtension extends FrameworkExtension<BootstrapAppExtension> {

        static final ContextTemplate CIT = ContextTemplate.of(MethodHandles.lookup(), ApplicationLaunchContext.class, ApplicationLaunchContext.class);

        static final OperationTemplate Ot = OperationTemplate.raw().reconfigure(c -> c.inContext(CIT).returnTypeObject());

        static final BeanTemplate ZBT = new PackedBeanTemplate(BeanKind.UNMANAGED).withOperationTemplate(Ot);

        MethodHandle mh;

        <T> void newApplication(Class<T> hostBean) {
            // We need the attachment, because ContainerGuest is on
            BeanHandle<BeanConfiguration> h = base().newBean(ZBT).install(hostBean, BeanHandle::new);
            h.lifetimeOperations().get(0).generateMethodHandleOnCodegen(m -> mh = m);
        }

        <T> void newApplication(Op<T> hostBean) {
            BeanHandle<BeanConfiguration> h = base().newBean(ZBT).install(hostBean, BeanHandle::new);
            h.lifetimeOperations().get(0).generateMethodHandleOnCodegen(m -> mh = m);
        }
    }

    // Maybe we want to return a special mirror?
    // We keep it for now
    private static class BootstrapApplicationHandle<A> extends ApplicationHandle<ApplicationConfiguration, A> {

        public BootstrapApplicationHandle(Installer installer) {
            super(installer);
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

        /** The bootstrap app extension. */
        private BootstrapAppExtension bootstrapExtension;

        /** The {@link Op} or {@link Class} used for creating the application interface. */
        private final Object opOrClass;

        /** The template for the root container of the bootstrapped application. */
        private PackedContainerTemplateConfigurator template;

        private Composer(Object opOrClass, Class<?> type) {
            this.opOrClass = opOrClass;
            this.template = new PackedContainerTemplateConfigurator(new PackedContainerTemplate(PackedContainerKind.ROOT_UNMANAGED, type));
        }

        /**
         * Adds 1 or more container lifetime channels.
         *
         * @param channels
         *            the channel(s) to add
         * @return this composer
         */
        public Composer addChannel(ContainerTemplateLink... channels) {
            this.template = (PackedContainerTemplateConfigurator) template.withPack(channels);
            return this;
        }

        /**
         * @param resultType
         *            the type of result the
         * @return this composer
         */
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
//
//        public Composer wirelets(Wirelet... wirelets) {
//            this.template = template.withWirelets(wirelets);
//            return this;
//        }

        /** An composer wrapping Assembly. */
        private static class BootstrapAppAssembly extends ComposableAssembly<Composer> {

            private BootstrapAppAssembly(Composer c, ComposerAction<? super Composer> action) {
                super(c, action);
            }
        }
    }

}

interface BootstrapAppSandbox<A> {

    // Hvorfor ikke bruge BootstrapApp'en som en launcher???
    // Det betyder selv at vi altid skal chaine..
    // Men det er vel ok
    // map()->

    default Launcher<A> launcher() {
        throw new UnsupportedOperationException();
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
    default BootstrapApp<A> with(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
        // return new BootstrapApp<>(new BootstrapAppSetup(setup.mirrorSupplier, setup.template.withWirelets(wirelets),
        // setup.applicationLauncher));
    }

    /**
     * Creates a new bootstrap app that maps the application using the specified mapper.
     *
     * @param <E>
     *            the type to map the application to
     * @param mapper
     *            the application mapper
     * @return the new bootstrap app
     */
    default <E> BootstrapApp<E> map(Function<? super A, ? extends E> mapper) {
        throw new UnsupportedOperationException();
    }

    /**
     * A launcher is used before an application is launched.
     */
    public interface Launcher<A> {

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

    default BaseImage<A> with(Wirelet... wirelets) {
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
    static ApplicationMirror extractMirror(BaseImage<?> image) {
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

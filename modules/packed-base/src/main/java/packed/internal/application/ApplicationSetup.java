package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationRuntimeWirelets;
import app.packed.application.host.ApplicationHostMirror;
import app.packed.base.Nullable;
import app.packed.build.BuildTarget;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.state.sandbox.InstanceState;
import packed.internal.component.ComponentSetup;
import packed.internal.component.InternalWirelet;
import packed.internal.component.RealmSetup;
import packed.internal.component.bean.BeanSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolAccessor;

/** Build-time configuration of an application. */
public final class ApplicationSetup extends ContainerSetup {

    public final PackedApplicationDriver<?> applicationDriver;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    /**
     * The launch mode of the application. May be updated via usage of
     * {@link ApplicationRuntimeWirelets#launchMode(InstanceState)} at build-time. If used from an image
     * {@link ApplicationLaunchContext#launchMode} is updated instead.
     */
    InstanceState launchMode;

    // sync entrypoint
    @Nullable
    private MainThreadOfControl mainThread;

    /** The index of the application's runtime in the constant pool, or -1 if the application has no runtime, */
    @Nullable
    final PoolAccessor runtimeAccessor;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    ApplicationSetup(BuildSetup build, RealmSetup realm, PackedApplicationDriver<?> driver, Wirelet[] wirelets) {
        super(build, realm, new LifetimeSetup(null), driver, null, wirelets);
        this.applicationDriver = driver;
        this.launchMode = requireNonNull(driver.launchMode());

        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool
        this.runtimeAccessor = driver.hasRuntime() ? lifetime.pool.reserve(PackedApplicationRuntimeExtensor.class) : null;
    }

    public boolean hasMain() {
        return mainThread != null;
    }

    public boolean hasRuntime() {
        return applicationDriver.hasRuntime();
    }

    /** {@return whether or not the application is part of an image}. */
    public boolean isImage() {
        // TODO fix for multi-apps, We should probably take a @Nullable ApplicationHostSetup in the constructor
        return build.target == BuildTarget.IMAGE;
    }

    public MainThreadOfControl mainThread() {
        MainThreadOfControl m = mainThread;
        if (m == null) {
            m = mainThread = new MainThreadOfControl();
        }
        return m;
    }

    /** {@return an application adaptor that can be exposed to end-users} */
    public BuildTimeApplicationMirror applicationMirror() {
        return new BuildTimeApplicationMirror();
    }

    /**
     * A wirelet that will set the launch mode of the application. Used by
     * {@link ApplicationRuntimeWirelets#launchMode(InstanceState)}.
     */
    public static final class ApplicationLaunchModeWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        private final InstanceState launchMode;

        /**
         * Creates a new name wirelet
         * 
         * @param launchMode
         *            the new launch mode of the application
         */
        public ApplicationLaunchModeWirelet(InstanceState launchMode) {
            this.launchMode = requireNonNull(launchMode, "launchMode is null");
            if (launchMode == InstanceState.UNINITIALIZED) {
                throw new IllegalArgumentException(InstanceState.UNINITIALIZED + " is not a valid launch mode");
            }
        }

        @Override
        protected <T> PackedApplicationDriver<T> onApplicationDriver(PackedApplicationDriver<T> driver) {
            if (driver.launchMode() == launchMode) {
                return driver;
            }
            return super.onApplicationDriver(driver);
        }

        /** {@inheritDoc} */
        @Override
        protected void onBuild(ComponentSetup component) {
            // TODO we probably need to check that it is launchable
            checkIsApplication(component).launchMode = launchMode; // override any existing launch mode
        }

        /** {@inheritDoc} */
        @Override
        public void onImageInstantiation(ComponentSetup component, ApplicationLaunchContext launch) {
            // TODO we probably need to check that it is launchable
            launch.launchMode = launchMode;
        }
    }

    /** An application mirror adaptor. */
    private final class BuildTimeApplicationMirror implements ApplicationMirror {

        /** {@inheritDoc} */
        @Override
        public ComponentMirror component(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Set<Class<? extends Extension>> disabledExtensions() {
            // TODO add additional dsiabled extensions
            return ApplicationSetup.this.applicationDriver.bannedExtensions();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasRuntime() {
            return ApplicationSetup.this.applicationDriver.hasRuntime();
        }

        @Override
        public Optional<ApplicationHostMirror> host() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStronglyWired() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Module module() {
            return ApplicationSetup.this.realm.realmType().getModule();
        }

        @Override
        public ContainerMirror container() {
            return ApplicationSetup.this.container.mirror();
        }
    }

    public class MainThreadOfControl {
        public BeanSetup cs;

        public boolean isStatic;

        public MethodHandle methodHandle;

        public boolean hasExecutionBlock() {
            return methodHandle != null;
        }
    }
}

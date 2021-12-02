package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Set;

import app.packed.application.ApplicationDescriptor;
import app.packed.application.ApplicationDescriptor.ApplicationBuildType;
import app.packed.application.ApplicationLaunchMode;
import app.packed.application.ApplicationMirror;
import app.packed.application.ExecutionWirelets;
import app.packed.base.Nullable;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.lifecycle.RunState;
import packed.internal.bundle.ContainerSetup;
import packed.internal.bundle.PackedBundleDriver;
import packed.internal.component.RealmSetup;
import packed.internal.component.bean.BeanSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolAccessor;

/** Build-time configuration of an application. */
public final class ApplicationSetup {

    public final ApplicationDescriptor descriptor;

    /** */
    public final PackedApplicationDriver<?> applicationDriver;

    /** The build the application is a part of. */
    public final BuildSetup build;

    /** What we are building. */
    public final ApplicationBuildType buildKind;

    /** The root container of the application. Created in the constructor of this class. */
    public final ContainerSetup container;

    public final boolean hasRuntime;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    /**
     * The launch mode of the application. May be updated via usage of {@link ExecutionWirelets#launchMode(RunState)}
     * at build-time. If used from an image {@link ApplicationLaunchContext#launchMode} is updated instead.
     */
    final ApplicationLaunchMode launchMode;

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
    ApplicationSetup(BuildSetup build, ApplicationBuildType buildKind, RealmSetup realm, PackedApplicationDriver<?> driver, Wirelet[] wirelets) {
        this.build = requireNonNull(build);
        this.buildKind = buildKind;
        this.applicationDriver = driver;
        this.launchMode = requireNonNull(driver.launchMode());

        this.hasRuntime = driver.isExecutable();

        this.descriptor = new PackedApplicationDescriptor();

        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool
        this.container = new ContainerSetup(this, realm, new LifetimeSetup(null), /* fixme */ PackedBundleDriver.DRIVER, null, wirelets);
        this.runtimeAccessor = driver.isExecutable() ? container.lifetime.pool.reserve(PackedApplicationRuntimeExtensor.class) : null;
    }

    public boolean hasMain() {
        return mainThread != null;
    }

    public MainThreadOfControl mainThread() {
        MainThreadOfControl m = mainThread;
        if (m == null) {
            m = mainThread = new MainThreadOfControl();
        }
        return m;
    }

    /** {@return a build-time application mirror that can be exposed to end-users} */
    public ApplicationMirror mirror() {
        return new BuildTimeApplicationMirror();
    }

    /** An application mirror adaptor. */
    private final class BuildTimeApplicationMirror implements ApplicationMirror {

        @Override
        public ContainerMirror container() {
            return ApplicationSetup.this.container.mirror();
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
            return ApplicationSetup.this.hasRuntime;
        }

        /** {@inheritDoc} */
        @Override
        public Module module() {
            return ApplicationSetup.this.container.realm.realmType().getModule();
        }

        @Override
        public ApplicationDescriptor descriptor() {
            return ApplicationSetup.this.descriptor;
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

///**
//* A wirelet that will set the launch mode of the application. Used by
//* {@link ExecutionWirelets#launchMode(InstanceState)}.
//*/
//public static final class ApplicationLaunchModeWirelet extends InternalWirelet {
//
//  /** The (validated) name to override with. */
//  private final ApplicationLaunchMode launchMode;
//
//  /**
//   * Creates a new name wirelet
//   * 
//   * @param launchMode
//   *            the new launch mode of the application
//   */
//  public ApplicationLaunchModeWirelet(ApplicationLaunchMode launchMode) {
//      this.launchMode = requireNonNull(launchMode, "launchMode is null");
//     
//  }
//
//  @Override
//  protected <T> PackedApplicationDriver<T> onApplicationDriver(PackedApplicationDriver<T> driver) {
//      if (driver.launchMode() == launchMode) {
//          return driver;
//      }
//      return super.onApplicationDriver(driver);
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  protected void onBuild(ContainerSetup component) {
//      // TODO we probably need to check that it is launchable
//      checkIsApplication(component).launchMode = launchMode; // override any existing launch mode
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  public void onImageInstantiation(ContainerSetup component, ApplicationLaunchContext launch) {
//      // TODO we probably need to check that it is launchable
//      launch.launchMode = launchMode;
//  }
//}

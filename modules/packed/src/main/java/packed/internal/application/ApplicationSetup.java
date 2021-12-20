package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import app.packed.application.ApplicationDescriptor;
import app.packed.application.ApplicationDescriptor.ApplicationBuildType;
import app.packed.application.ApplicationMirror;
import app.packed.application.ExecutionWirelets;
import app.packed.base.Nullable;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.lifecycle.RunState;
import packed.internal.component.RealmSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedBundleDriver;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolAccessor;

/** Build-time configuration of an application. */
public final class ApplicationSetup {

    /** The driver responsible for building the application. */
    public final PackedApplicationDriver<?> applicationDriver;

    /** The build the application is a part of. */
    public final BuildSetup build;

    /** The root container of the application (created in the constructor of this class). */
    public final ContainerSetup container;

    public final ApplicationDescriptor descriptor;

    /** Entry points in the application, is null if there are none. */
    @Nullable
    public final EntryPointSetup entryPoints = new EntryPointSetup();

    /**
     * The launch mode of the application. May be updated via usage of {@link ExecutionWirelets#launchMode(RunState)} at
     * build-time. If used from an image {@link ApplicationInitializationContext#launchMode} is updated instead.
     */
    final RunState launchMode;
    
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
        this.applicationDriver = driver;
        this.launchMode = requireNonNull(driver.launchMode());

        this.descriptor = new PackedApplicationDescriptor(buildKind);

        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool
        
        this.container = new ContainerSetup(this, realm, new LifetimeSetup(null), /* fixme */ PackedBundleDriver.DRIVER, null, wirelets);
        this.runtimeAccessor = driver.isExecutable() ? container.lifetime.pool.reserve(PackedApplicationRuntime.class) : null;
    }

    /** {@return a build-time application mirror that can be exposed to end-users} */
    public ApplicationMirror mirror() {
        return new BuildTimeApplicationMirror(this);
    }
    
    /** An application mirror adaptor. */
    private record BuildTimeApplicationMirror(ApplicationSetup s) implements ApplicationMirror {

        @Override
        public ContainerMirror container() {
            return s.container.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Set<Class<? extends Extension>> disabledExtensions() {
            // TODO add additional dsiabled extensions
            return s.applicationDriver.bannedExtensions();
        }

        /** {@inheritDoc} */
        @Override
        public Module module() {
            return s.container.realm.realmType().getModule();
        }

        @Override
        public ApplicationDescriptor descriptor() {
            return s.descriptor;
        }

        /** {@inheritDoc} */
        @Override
        public <T extends ExtensionMirror> T use(Class<T> type) {
            return container().useExtension(type);
        }
    }
}

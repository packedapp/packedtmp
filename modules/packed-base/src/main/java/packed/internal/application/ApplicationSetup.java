package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Optional;

import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationWirelets;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.mirror.TreeWalker;
import app.packed.state.sandbox.InstanceState;
import packed.internal.component.ComponentSetup;
import packed.internal.component.InternalWirelet;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.SourcedComponentSetup;
import packed.internal.component.WireableComponentDriver.ContainerComponentDriver;
import packed.internal.container.ContainerSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;

/** Build-time configuration of an application. */
public final class ApplicationSetup {

    public final BuildSetup build;

    /** The configuration of the main constant build. */
    public final ConstantPoolSetup constantPool = new ConstantPoolSetup();

    /** The root container of the application. */
    public final ContainerSetup container;

    /** The driver of the application. */
    public final PackedApplicationDriver<?> driver;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    /**
     * The launch mode of the application. May be updated via usage of {@link ApplicationWirelets#launchMode(InstanceState)}
     * at build-time. If used from an image {@link ApplicationLaunchContext#launchMode} is updated instead.
     */
    InstanceState launchMode;

    // sync entrypoint
    @Nullable
    private MainThreadOfControl mainThread;

    private final int modifiers;

    final int runtimePoolIndex;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    ApplicationSetup(BuildSetup build, PackedApplicationDriver<?> driver, RealmSetup realm, ContainerComponentDriver containerDriver, int modifiers,
            Wirelet[] wirelets) {
        this.build = requireNonNull(build);
        this.driver = requireNonNull(driver, "driver is null");
        this.launchMode = requireNonNull(driver.launchMode());
        this.container = containerDriver.newComponent(this, realm, null, wirelets);
        this.modifiers = modifiers;

        // Setup Runtime if needed
        if (container.modifiers().hasRuntime()) {
            runtimePoolIndex = constantPool.reserveObject(); // reserve a slot to an instance of PackedApplicationRuntime
        } else {
            runtimePoolIndex = -1;
        }
    }

    public boolean hasMain() {
        return mainThread != null;
    }

    /** {@return whether or not the application is part of an image}. */
    public boolean isImage() {
        return PackedComponentModifierSet.isImage(modifiers);
    }

    public MainThreadOfControl mainThread() {
        MainThreadOfControl m = mainThread;
        if (m == null) {
            m = mainThread = new MainThreadOfControl();
        }
        return m;
    }

    /** {@return an application adaptor that can be exposed to end-users} */
    public ApplicationMirror mirror() {
        return new ApplicationMirrorAdaptor(this);
    }

    /**
     * A wirelet that will set the launch mode of the application. Used by
     * {@link ApplicationWirelets#launchMode(InstanceState)}.
     */
    public static final class ApplicationLaunchModeWirelet extends InternalWirelet {

        @Override
        protected <T> PackedApplicationDriver<T> onApplicationDriver(PackedApplicationDriver<T> driver) {
            if (driver.launchMode() == launchMode) {
                return driver;
            }
            return super.onApplicationDriver(driver);
        }

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

    public class MainThreadOfControl {
        public SourcedComponentSetup cs;

        public boolean isStatic;

        public MethodHandle methodHandle;

        public boolean hasExecutionBlock() {
            return methodHandle != null;
        }
    }

    /** An adaptor of {@link ApplicationSetup} exposed as {@link ApplicationMirror}. */
    private /* primitive */ record ApplicationMirrorAdaptor(ApplicationSetup application) implements ApplicationMirror {

        /** {@inheritDoc} */
        @Override
        public ComponentMirror component() {
            return application.container.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public ContainerMirror container() {
            return application.container.containerMirror();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return application.container.getName();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStronglyWired() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<ApplicationMirror> parent() {
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isRunnable() {
            return application.driver.hasRuntime();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentMirror component(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public TreeWalker<ComponentMirror> components() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ContainerMirror container(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public TreeWalker<ContainerMirror> containers() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public NamespacePath path() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Module module() {
            return application.container.realm.realmType().getModule();
        }
    }
}

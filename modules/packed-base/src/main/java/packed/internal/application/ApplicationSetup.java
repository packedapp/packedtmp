package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import app.packed.application.Application;
import app.packed.application.ApplicationWirelets;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.Wirelet;
import app.packed.container.Container;
import app.packed.state.RunState;
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

    /** The configuration of the main constant build. */
    public final ConstantPoolSetup constantPool = new ConstantPoolSetup();

    /** The root container of the application. */
    public final ContainerSetup container;

    /** The driver of the applications. */
    public final PackedApplicationDriver<?> driver;

    /**
     * The launch mode of the application. May be updated via usage of {@link ApplicationWirelets#launchMode(RunState)},
     * both at build-time and if launched from an image.
     */
    RunState launchMode;

    // sync entrypoint
    @Nullable
    private MainThreadOfControl mainThread;

    private final int modifiers;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    final int runtimePoolIndex;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    ApplicationSetup(BuildSetup build, PackedApplicationDriver<?> driver, RealmSetup realm, ContainerComponentDriver containerDriver, int modifiers,
            Wirelet[] wirelets) {
        this.driver = requireNonNull(driver, "driver is null");
        this.container = containerDriver.newComponent(build, this, realm, null, wirelets);
        this.modifiers = modifiers;
        // Setup Runtime
        if (container.modifiers().hasRuntime()) {
            runtimePoolIndex = constantPool.reserve(); // reserve a slot to an instance of PackedApplicationRuntime
        } else {
            runtimePoolIndex = -1;
        }
    }

    /** {@return returns an Application adaptor that can be exposed to end-users} */
    public Application adaptor() {
        return new Adaptor(this);
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

    /** {@return whether or not the application is part of an image}. */
    public boolean isImage() {
        return PackedComponentModifierSet.isImage(modifiers);
    }

    /**
     * A wirelet that will set the launch mode of the application. Used by {@link ApplicationWirelets#launchMode(RunState)}.
     */
    public static final class ApplicationLaunchModeWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        private final RunState launchMode;

        /**
         * Creates a new name wirelet
         * 
         * @param launchMode
         *            the new launch mode of the application
         */
        public ApplicationLaunchModeWirelet(RunState launchMode) {
            this.launchMode = requireNonNull(launchMode, "launchMode is null");
            if (launchMode == RunState.INITIALIZING) {
                throw new IllegalArgumentException(RunState.INITIALIZING + " is not a valid launch mode");
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void onBuild(ComponentSetup component) {
            checkIsApplication(component).launchMode = launchMode;
        }

        /** {@inheritDoc} */
        @Override
        public void onImageInstantiation(ComponentSetup component, ApplicationLaunchContext ic) {
            ic.launchMode = launchMode;
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

    /** An adaptor of {@link ApplicationSetup} exposed as {@link Application}. */
    private record Adaptor(ApplicationSetup application) implements Application {

        /** {@inheritDoc} */
        @Override
        public Component component() {
            return application.container.adaptor();
        }

        /** {@inheritDoc} */
        @Override
        public Container container() {
            return application.container.containerAdaptor();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return application.container.getName();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStronglyWired() {
            throw new UnsupportedOperationException();
        }
    }
}

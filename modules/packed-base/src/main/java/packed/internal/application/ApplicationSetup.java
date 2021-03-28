package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.application.Application;
import app.packed.component.Component;
import app.packed.component.Wirelet;
import app.packed.container.Container;
import app.packed.state.RunState;
import packed.internal.component.ComponentSetup;
import packed.internal.component.InternalWirelet;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.WireableComponentDriver;
import packed.internal.component.source.SourceComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;

/** Build-time configuration for an application. */
public final class ApplicationSetup {

    final ContainerSetup container;

    /** The configuration of the main constant build. */
    public final ConstantPoolSetup constantPool = new ConstantPoolSetup();

    /** The applications's driver. */
    public final PackedApplicationDriver<?> driver;

    RunState launchMode;

    public final Lifecycle lifecycle = new Lifecycle();

    private final int modifiers;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    ApplicationSetup(BuildSetup build, PackedApplicationDriver<?> driver, RealmSetup realm, WireableComponentDriver<?> componentDriver, int modifiers,
            Wirelet[] wirelets) {
        this.driver = requireNonNull(driver, "driver is null");
        if (!componentDriver.modifiers().isContainer()) {
            throw new IllegalArgumentException("Can only create an application using a container component driver");
        }
        container = (ContainerSetup) componentDriver.newComponent(build, this, realm, null, wirelets);
        this.modifiers = modifiers;
    }

    /**
     * @return
     */
    public Application adaptor() {
        return new Adaptor(this);
    }

    public boolean hasMain() {
        return lifecycle.methodHandle != null;
    }

    public boolean isImage() {
        return PackedComponentModifierSet.isImage(modifiers);
    }

    public RunState launchMode() {
        return launchMode;
    }

    /** A wirelet that will set the name of the component. Used by {@link Wirelet#named(String)}. */
    public static final class ApplicationLaunchModeWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        private final RunState launchMode;

        /**
         * Creates a new name wirelet
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public ApplicationLaunchModeWirelet(RunState launchMode) {
            this.launchMode = requireNonNull(launchMode, "launchMode is null");
            if (launchMode == RunState.INITIALIZING) {
                throw new IllegalArgumentException(RunState.INITIALIZING + " is not a valid launch mode");
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void onBuild(ComponentSetup c) {
            checkApplication(c).launchMode = launchMode;
        }

        @Override
        public void onImageInstantiation(ComponentSetup c, ApplicationLaunchContext ic) {
            ic.launchMode = launchMode;
        }
    }

    public static class Lifecycle {
        public SourceComponentSetup cs;
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
            return application.container.name;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStronglyWired() {
            throw new UnsupportedOperationException();
        }
    }
}

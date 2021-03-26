package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.component.ComponentDriver;
import packed.internal.component.BaseComponentSetup;

/** Build-time configuration for an application. */
public final class ApplicationSetup {

    /** The applications's driver. */
    public final PackedApplicationDriver<?> driver;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    ApplicationSetup(PackedApplicationDriver<?> driver, ComponentDriver<?> componentDriver) {
        this.driver = requireNonNull(driver, "driver is null");
        if (!componentDriver.modifiers().isContainer()) {
            throw new IllegalArgumentException("Can only create an application using a container component driver");
        }
    }

    public final Lifecycle lifecycle = new Lifecycle();

    public boolean hasMain() {
        return lifecycle.methodHandle != null;
    }

    public static class Lifecycle {
        public boolean isStatic;
        public boolean hasExecutionBlock() {
            return methodHandle != null;
        }

        public BaseComponentSetup cs;
        
        public MethodHandle methodHandle;
    }

}

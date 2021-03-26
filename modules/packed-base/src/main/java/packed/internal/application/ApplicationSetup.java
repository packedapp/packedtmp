package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.component.ComponentDriver;
import packed.internal.component.source.SourceComponentSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;

/** Build-time configuration for an application. */
public final class ApplicationSetup {

    /** The applications's driver. */
    public final PackedApplicationDriver<?> driver;

    /** The configuration of the main constant build. */
    public final ConstantPoolSetup constantPool = new ConstantPoolSetup();
    
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

        public SourceComponentSetup cs;
        
        public MethodHandle methodHandle;
    }

}

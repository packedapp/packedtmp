package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.component.ComponentDriver;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.source.SourceComponentSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;

/** Build-time configuration for an application. */
public final class ApplicationSetup {

    /** The configuration of the main constant build. */
    public final ConstantPoolSetup constantPool = new ConstantPoolSetup();

    /** The applications's driver. */
    public final PackedApplicationDriver<?> driver;

    public final Lifecycle lifecycle = new Lifecycle();

    private final int modifiers;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    ApplicationSetup(PackedApplicationDriver<?> driver, ComponentDriver<?> componentDriver, int modifiers) {
        this.driver = requireNonNull(driver, "driver is null");
        if (!componentDriver.modifiers().isContainer()) {
            throw new IllegalArgumentException("Can only create an application using a container component driver");
        }
        this.modifiers = modifiers;
    }

    public boolean isImage() {
        return PackedComponentModifierSet.isImage(modifiers);
    }

    public boolean hasMain() {
        return lifecycle.methodHandle != null;
    }

    public static class Lifecycle {
        public SourceComponentSetup cs;
        public boolean isStatic;

        public MethodHandle methodHandle;

        public boolean hasExecutionBlock() {
            return methodHandle != null;
        }
    }

}

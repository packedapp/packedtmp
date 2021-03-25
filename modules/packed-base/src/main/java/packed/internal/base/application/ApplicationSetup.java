package packed.internal.base.application;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentDriver;

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
    ApplicationSetup(PackedApplicationDriver<?> driver,  ComponentDriver<?> componentDriver) {
        this.driver = requireNonNull(driver, "driver is null");
        if (!componentDriver.modifiers().isContainer()) {
            throw new IllegalArgumentException("Can only create an application using a container component driver");
        }
    }
}

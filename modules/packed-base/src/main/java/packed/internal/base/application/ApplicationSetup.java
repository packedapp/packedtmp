package packed.internal.base.application;

import static java.util.Objects.requireNonNull;

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
    ApplicationSetup(PackedApplicationDriver<?> driver) {
        this.driver = requireNonNull(driver, "driver is null");
    }
}

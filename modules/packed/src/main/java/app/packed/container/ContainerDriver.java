package app.packed.container;

import java.util.Set;

import app.packed.extension.Extension;
import packed.internal.container.PackedContainerDriver;

// Altsaa hvordan er det lige praecis ContainerConfiguration skal faa ekstra information?
// Vi er vist enige om at vi ikke gider kunne overskrive den, bare for at 
// Tror ContainerConfiguration doer, lad os se hvad vi goer med ApplicationDriver

//// Altsaa den er brugbart, hvis vi supportere (mest for extensions) at vi kalder
// ContainerConfiguration newContainer(ContainerDriver<?> driver);
//  T newContainer(ContainerDriver<T> driver);

public abstract sealed class ContainerDriver permits PackedContainerDriver {

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    public abstract Set<Class<? extends Extension<?>>> bannedExtensions();

    /** {@return creates a new configuration object, only used by the PackedContainerDriver.} */
    protected ContainerConfiguration newConfiguration() {
        return new ContainerConfiguration();
    }

    /** {@return the default driver that is used to configure containers.} */
    public static ContainerDriver defaultDriver() {
        return PackedContainerDriver.DEFAULT;
    }

    /** {@return a special driver that is used to configure containers.} */
    // Maaske er den ikke public???...
    public static ContainerDriver embed() {
        return PackedContainerDriver.DEFAULT;
    }

    interface Builder {
        ContainerDriver build();
    }
}

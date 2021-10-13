package app.packed.bundle;

import java.util.Set;

import app.packed.extension.Extension;
import packed.internal.bundle.PackedBundleDriver;

// Skal end users kunne lave dem her??? Tanker det umiddelbart ikke...
// Lidt som de heller ikke skal lave bean drivers...

// Altsaa hvordan er det lige praecis ContainerConfiguration skal faa ekstra information?
// Vi er vist enige om at vi ikke gider kunne overskrive den, bare for at 
// Tror ContainerConfiguration doer, lad os se hvad vi goer med ApplicationDriver

//// Altsaa den er brugbart, hvis vi supportere (mest for extensions) at vi kalder
// ContainerConfiguration newContainer(ContainerDriver<?> driver);
//  T newContainer(ContainerDriver<T> driver);

public sealed interface BundleDriver permits PackedBundleDriver {

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    Set<Class<? extends Extension>> bannedExtensions();

    /** {@return the default driver that is used to configure containers.} */
    public static BundleDriver defaultDriver() {
        return PackedBundleDriver.DRIVER;
    }

    interface Builder {
        BundleDriver build();
    }
}

// manglende funktionalitet. Muligheden for at returnere et specifikt ContainerMirror
// Tror det kraever en builder. Hmm usecases???

//2 valgmuligheder

//1. som nu 
//2. Lade brugere overskrive den

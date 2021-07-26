package app.packed.container;

import java.util.Set;
import java.util.function.Supplier;

import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.extension.Extension;
import packed.internal.container.PackedContainerDriver;

//2 valgmuligheder

// 1. som nu 
// 2. Lade brugere overskrive den

// Altsaa hvordan er det lige praecis ContainerConfiguration skal faa ekstra information?
// Vi er vist enige om at vi ikke gider kunne overskrive den, bare for at 

public interface ContainerDriver<C extends ContainerConfiguration> extends ComponentDriver<C> {

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    Set<Class<? extends Extension>> disabledExtensions();

    @Override
    ContainerDriver<C> with(Wirelet... wirelets);

    /** {@return the default driver that is used to configure containers.} */
    public static ContainerDriver<ContainerConfiguration> defaultDriver() {
        return PackedContainerDriver.DRIVER;
    }

    static <C extends ContainerConfiguration> ContainerDriver<C> of(Supplier<C> configurationFactory, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
    
    interface Builder {
        ContainerDriver<ContainerConfiguration> build();
    }
}
// manglende funktionalitet. Muligheden for at returnere et specifikt ContainerMirror
// Tror det kraever en builder. 
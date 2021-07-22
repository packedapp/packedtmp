package app.packed.container;

import java.util.Set;
import java.util.function.Supplier;

import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.extension.Extension;
import packed.internal.container.PackedContainerDriver;

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

    
    /**
     * Returns a driver for creating new containers.
     * 
     * @return a driver for creating new containers
     */
    public static ContainerDriver<ContainerConfiguration> defaultDriver() {
        return PackedContainerDriver.DRIVER;
    }
    
    static <C extends ContainerConfiguration> ContainerDriver<C> of(Supplier<C> configurationFactory, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static ContainerDriver<ContainerConfiguration> of(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}

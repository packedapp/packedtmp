package app.packed.container;

import java.util.Set;

import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.extension.Extension;

public interface ContainerDriver<C extends ContainerConfiguration> extends ComponentDriver<C> {

    /**
     * Returns an immutable set containing any extensions that have been disabled.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * <p>
     * 
     * @return a set of disabled extensions
     */
    Set<Class<? extends Extension>> disabledExtensions();
    
    @Override
    ContainerDriver<C> with(Wirelet... wirelet);
}

package app.packed.container;

import java.util.Set;

import app.packed.extension.Extension;
import packed.internal.container.PackedContainerHandle;

public sealed interface ContainerHandle permits PackedContainerHandle {

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    public abstract Set<Class<? extends Extension<?>>> bannedExtensions();
}

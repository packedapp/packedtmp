package internal.app.packed.container;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.Extension;

/** Implementation of {@link ContainerHandle}. */
public final class PackedContainerDriver implements ContainerHandle {

    @Nullable
    final ContainerSetup parent;

    public ContainerSetup setup;

    public PackedContainerDriver(@Nullable ContainerSetup parent) {
        this.parent = parent;
    }

    @Override
    public Set<Class<? extends Extension<?>>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    public final ContainerConfiguration toConfiguration(ContainerSetup component) {
        this.setup = component;
        ContainerConfiguration c = new ContainerConfiguration(this);
        return c;
    }
}
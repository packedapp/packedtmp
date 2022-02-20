package packed.internal.container;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.extension.Extension;

/** Implementation of {@link ContainerDriver}. */
public final class PackedContainerDriver implements ContainerDriver {

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
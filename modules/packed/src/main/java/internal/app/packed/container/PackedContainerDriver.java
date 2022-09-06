package internal.app.packed.container;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerCustomizer;
import app.packed.container.Extension;

/** Implementation of {@link ContainerCustomizer}. */
public final class PackedContainerDriver implements ContainerCustomizer {

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
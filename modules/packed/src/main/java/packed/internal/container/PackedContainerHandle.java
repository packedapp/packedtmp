package packed.internal.container;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerCustomizer;
import app.packed.extension.Extension;

/** A special component driver that create containers. */
public final class PackedContainerHandle implements ContainerCustomizer {

    @Nullable
    final ContainerSetup parent;

    public PackedContainerHandle(@Nullable ContainerSetup parent) {
        this.parent = parent;
    }

    public ContainerSetup setup;

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
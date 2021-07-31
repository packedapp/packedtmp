package packed.internal.container;

import java.util.Set;

import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.extension.Extension;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;

/** A special component driver that create containers. */
public non-sealed class PackedContainerDriver<C extends ContainerConfiguration> extends PackedComponentDriver<C> implements ContainerDriver<C> {

    /** A driver for configuring containers. */
    public static final PackedContainerDriver<ContainerConfiguration> DRIVER = new PackedContainerDriver<>();

    @Override
    public Set<Class<? extends Extension>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public C toConfiguration0(ComponentSetup context) {
        return (C) new ContainerConfiguration();
    }

}
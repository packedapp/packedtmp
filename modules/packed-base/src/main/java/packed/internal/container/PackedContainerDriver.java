package packed.internal.container;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.extension.Extension;
import packed.internal.application.BuildSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.RealmSetup;
import packed.internal.lifetime.LifetimeSetup;

/** A special component driver that create containers. */
public class PackedContainerDriver<C extends ContainerConfiguration> extends PackedComponentDriver<C> implements ContainerDriver<C> {

    /** A driver for configuring containers. */
    public static final ContainerDriver<ContainerConfiguration> DRIVER = new PackedContainerDriver<>(null);

    public PackedContainerDriver(@Nullable Wirelet wirelet) {
        super(wirelet);
    }

    @Override
    public Set<Class<? extends Extension>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup newComponent(BuildSetup build, RealmSetup realm, LifetimeSetup lifetime, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        return new ContainerSetup(build, realm, lifetime, this, parent, wirelets);
    }

    @SuppressWarnings("unchecked")
    @Override
    public C toConfiguration0(ComponentSetup context) {
        return (C) new ContainerConfiguration();
    }

}
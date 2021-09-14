package packed.internal.container;

import java.util.Optional;
import java.util.Set;

import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.BundleDriver;
import app.packed.extension.Extension;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;

/** A special component driver that create containers. */
public final class PackedContainerDriver<C extends BundleConfiguration> extends PackedComponentDriver<C> implements BundleDriver<C> {

    /** A driver for configuring containers. */
    public static final PackedContainerDriver<BundleConfiguration> DRIVER = new PackedContainerDriver<>();

    @Override
    public Set<Class<? extends Extension>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public C toConfiguration0(ComponentSetup context) {
        return (C) new BundleConfiguration();
    }

    @Override
    public Optional<Class<? extends Extension>> extension() {
        throw new UnsupportedOperationException();
    }

}
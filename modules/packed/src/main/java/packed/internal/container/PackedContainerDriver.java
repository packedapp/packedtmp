package packed.internal.container;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Set;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.extension.Extension;
import packed.internal.component.ComponentSetup;
import packed.internal.util.LookupUtil;

/** A special component driver that create containers. */
public final class PackedContainerDriver extends ContainerDriver {

    /** A driver for configuring containers. */
    public static final PackedContainerDriver DEFAULT = new PackedContainerDriver();

    /** A handle that can access ComponentConfiguration#component. */
    private static final VarHandle VH_COMPONENT_CONFIGURATION_COMPONENT = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(),
            ComponentConfiguration.class, "component", ComponentSetup.class);

    @Override
    public Set<Class<? extends Extension<?>>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    public final ContainerConfiguration toConfiguration(ContainerSetup component) {
        ContainerConfiguration c = newConfiguration();
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(c, component);
        return c;
    }
}
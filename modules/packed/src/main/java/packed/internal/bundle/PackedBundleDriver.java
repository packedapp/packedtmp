package packed.internal.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import java.util.Set;

import app.packed.bundle.BundleAssembly;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.BundleDriver;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;
import packed.internal.component.ComponentSetup;
import packed.internal.util.LookupUtil;

/** A special component driver that create containers. */
public final class PackedBundleDriver implements BundleDriver {

    /** A driver for configuring containers. */
    public static final PackedBundleDriver DRIVER = new PackedBundleDriver();

    /** A handle that can access Assembly#driver. */
    private static final VarHandle VH_ASSEMBLY_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BundleAssembly.class, "driver",
            PackedBundleDriver.class);

    /** A handle that can access ComponentConfiguration#component. */
    private static final VarHandle VH_COMPONENT_CONFIGURATION_COMPONENT = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(),
            ComponentConfiguration.class, "component", ComponentSetup.class);

    @Override
    public Set<Class<? extends Extension>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Class<? extends Extension>> extension() {
        throw new UnsupportedOperationException();
    }

    public final BundleConfiguration toConfiguration(ComponentSetup cs) {
        BundleConfiguration c = toConfiguration0(cs);
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(c, cs);
        return c;
    }
    public BundleConfiguration toConfiguration0(ComponentSetup context) {
        return new BundleConfiguration();
    }

    /**
     * Extracts the component driver from the specified assembly.
     * 
     * @param assembly
     *            the assembly to extract the component driver from
     * @return the component driver of the specified assembly
     */
    public static PackedBundleDriver getDriver(BundleAssembly assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedBundleDriver) VH_ASSEMBLY_DRIVER.get(assembly);
    }
}
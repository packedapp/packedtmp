package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.bundle.Bundle;
import app.packed.component.ComponentConfiguration;
import packed.internal.container.PackedContainerDriver;
import packed.internal.util.LookupUtil;

/** The abstract base class for component drivers. */
public abstract class PackedComponentDriver<C extends ComponentConfiguration> {

    /** A handle that can access Assembly#driver. */
    private static final VarHandle VH_ASSEMBLY_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Bundle.class, "driver",
            PackedContainerDriver.class);

    /** A handle that can access ComponentConfiguration#component. */
    private static final VarHandle VH_COMPONENT_CONFIGURATION_COMPONENT = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(),
            ComponentConfiguration.class, "component", ComponentSetup.class);

    public final C toConfiguration(ComponentSetup cs) {
        C c = toConfiguration0(cs);
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(c, cs);
        return c;
    }
    
    public abstract C toConfiguration0(ComponentSetup context);


    /**
     * Extracts the component driver from the specified assembly.
     * 
     * @param assembly
     *            the assembly to extract the component driver from
     * @return the component driver of the specified assembly
     */
    public static PackedComponentDriver<?> getDriver(Bundle<?> assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedComponentDriver<?>) VH_ASSEMBLY_DRIVER.get(assembly);
    }
}

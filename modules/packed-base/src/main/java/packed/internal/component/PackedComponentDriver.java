package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import packed.internal.util.LookupUtil;

public abstract class PackedComponentDriver<C extends ComponentConfiguration> implements ComponentDriver<C> {

    /** A handle that can access Assembly#driver. */
    private static final VarHandle VH_ASSEMBLY_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "driver",
            SourcedComponentDriver.class);

    @Nullable
    final Wirelet wirelet;
    public final int modifiers;

    public PackedComponentDriver(Wirelet wirelet, int modifiers) {
        this.wirelet = wirelet;
        this.modifiers = modifiers;
    }

    public void checkBound() {}

    public abstract C toConfiguration(ComponentConfigurationContext context);

    /** {@inheritDoc} */
    @Override
    public ComponentDriver<C> with(Wirelet... wirelets) {
        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
        return withWirelet(w);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentDriver<C> with(Wirelet wirelet) {
        requireNonNull(wirelet, "wirelet is null");
        Wirelet w = this.wirelet == null ? wirelet : wirelet.andThen(wirelet);
        return withWirelet(w);
    }

    protected abstract ComponentDriver<C> withWirelet(Wirelet w);

    /**
     * Extracts the component driver from the specified assembly.
     * 
     * @param assembly
     *            the assembly to extract the component driver from
     * @return the component driver of the specified assembly
     */
    public static <C extends ComponentConfiguration> PackedComponentDriver<? extends C> getDriver(Assembly<C> assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedComponentDriver<? extends C>) VH_ASSEMBLY_DRIVER.get(assembly);
    }

}

package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.extension.Extension;
import packed.internal.application.BuildSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.LookupUtil;

/** The abstract base class for component drivers. */
public abstract class PackedComponentDriver<C extends ComponentConfiguration> implements ComponentDriver<C> {

    /** A handle that can access Assembly#driver. */
    private static final VarHandle VH_ASSEMBLY_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "driver",
            PackedComponentDriver.class);

    /** A handle that can access ComponentConfiguration#component. */
    private static final VarHandle VH_COMPONENT_CONFIGURATION_COMPONENT = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(),
            ComponentConfiguration.class, "component", ComponentSetup.class);

    /** Optional wirelets that will be applied to any component created by this driver. */
    @Nullable
    protected final Wirelet wirelet;

    protected PackedComponentDriver(@Nullable Wirelet wirelet) {
        this.wirelet = wirelet;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<? extends Extension>> extension() {
        throw new UnsupportedOperationException();
    }

    protected abstract ComponentSetup newComponent(BuildSetup build, RealmSetup realm, LifetimeSetup lifetime, @Nullable ComponentSetup parent,
            Wirelet[] wirelets);

    public final C toConfiguration(ComponentSetup cs) {
        C c = toConfiguration0(cs);
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(c, cs);
        return c;
    }
    
    public abstract C toConfiguration0(ComponentSetup context);

//    /** {@inheritDoc} */
//    @Override
//    public ComponentDriver<C> with(Wirelet... wirelets) {
//        // Vi kan faktisks godt lave nogle checks allerede her
//        // Application Wirelets kan f.eks. kun bindes til en ContainerComponentDriver.
//        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
//        return withWirelet(w);
//    }

    protected abstract ComponentDriver<C> withWirelet(Wirelet w);

    /**
     * Extracts the component driver from the specified assembly.
     * 
     * @param assembly
     *            the assembly to extract the component driver from
     * @return the component driver of the specified assembly
     */
    public static PackedComponentDriver<?> getDriver(Assembly<?> assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedComponentDriver<?>) VH_ASSEMBLY_DRIVER.get(assembly);
    }
}

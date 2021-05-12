package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import app.packed.container.ContainerConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.util.LookupUtil;

public abstract class PackedComponentDriver<C extends ComponentConfiguration> implements ComponentDriver<C> {

    /** A handle that can access Assembly#driver. */
    private static final VarHandle VH_ASSEMBLY_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "driver",
            PackedComponentDriver.class);

    public final int modifiers;

    /** Optional wirelets that will be applied to any component created by this driver. */
    @Nullable
    final Wirelet wirelet;

    public PackedComponentDriver(Wirelet wirelet, int modifiers) {
        this.wirelet = wirelet;
        this.modifiers = modifiers;
    }

    public void checkBound() {}

    public abstract ComponentSetup newComponent(ApplicationSetup application, RealmSetup realm, @Nullable ComponentSetup parent,
            Wirelet[] wirelets);

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
    public static PackedComponentDriver<?> getDriver(Assembly<?> assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedComponentDriver<?>) VH_ASSEMBLY_DRIVER.get(assembly);
    }

    /** A special component driver that create containers. */
    // Leger med tanken om at lave en specifik public interface container driver
    public static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        /** A component modifier set shared by every container. */
        private static final ComponentModifierSet CONTAINER_MODIFIERS = ComponentModifierSet.of(ComponentModifier.CONTAINER);

        public ContainerComponentDriver(Wirelet wirelet) {
            super(wirelet, PackedComponentModifierSet.I_CONTAINER);
        }

        @Override
        public final ComponentDriver<ContainerConfiguration> bind(Object object) {
            throw new UnsupportedOperationException("Cannot bind to a container component driver");
        }

        @Override
        public final ComponentModifierSet modifiers() {
            return CONTAINER_MODIFIERS;
        }

        /** {@inheritDoc} */
        public ContainerSetup newComponent(ApplicationSetup application, RealmSetup realm, @Nullable ComponentSetup parent,
                Wirelet[] wirelets) {
            return new ContainerSetup(application, realm, this, parent, wirelets);
        }
        @Override
        public ContainerConfiguration toConfiguration(ComponentConfigurationContext context) {
            return new ContainerConfiguration(context);
        }

        @Override
        protected ContainerComponentDriver withWirelet(Wirelet w) {
            return new ContainerComponentDriver(w);
        }
    }

}

package packed.internal.container;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.container.BaseContainerConfiguration;
import app.packed.container.ContainerConfiguration;
import packed.internal.application.BuildSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.RealmSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.LookupUtil;

/** A special component driver that create containers. */
// Leger med tanken om at lave en specifik public interface container driver
public class PackedContainerDriver extends PackedComponentDriver<BaseContainerConfiguration> {

    /** A handle that can access ContainerConfiguration#container. */
    private static final VarHandle VH_ABSTRACT_CONTAINER_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(),
            ContainerConfiguration.class, "container", ContainerSetup.class);

    public PackedContainerDriver(Wirelet wirelet) {
        super(wirelet);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup newComponent(BuildSetup build, RealmSetup realm, LifetimeSetup lifetime, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        return new ContainerSetup(build, realm, lifetime, this, parent, wirelets);
    }

    @Override
    public BaseContainerConfiguration toConfiguration(ComponentSetup context) {
        BaseContainerConfiguration cc = new BaseContainerConfiguration();
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(cc, context);
        VH_ABSTRACT_CONTAINER_CONFIGURATION.set(cc, context);
        return cc;
    }

    @Override
    protected PackedContainerDriver withWirelet(Wirelet w) {
        return new PackedContainerDriver(w);
    }

    @Override
    public ComponentDriver<BaseContainerConfiguration> with(Wirelet... wirelet) {
        throw new UnsupportedOperationException();
    }
}
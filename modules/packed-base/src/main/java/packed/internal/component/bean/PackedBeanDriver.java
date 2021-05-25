package packed.internal.component.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.component.BeanConfiguration;
import app.packed.component.BeanDriver;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import packed.internal.application.BuildSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.RealmSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link BeanDriver}. */
public final class PackedBeanDriver<C extends BeanConfiguration> extends PackedComponentDriver<C> implements BeanDriver<C> {

    /** The bean binder. Either a Class, Factory or instance. */
    final Object binding;

    final boolean isConstant;

    final MethodHandle configurationConstructor;

    /** The bean type. */
    private final Class<?> beanType;

    public PackedBeanDriver(PackedBeanDriverBinder<?, C> binder, Class<?> beanType, Object binding) {
        super(null);
        this.configurationConstructor = binder.constructor();
        this.binding = requireNonNull(binding);
        this.beanType = requireNonNull(beanType);
        this.isConstant = binder.isConstant();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> beanType() {
        return beanType;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentSetup newComponent(BuildSetup build, RealmSetup realm, LifetimeSetup lifetime, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        return new BeanSetup(build, lifetime, realm, this, parent, wirelets);
    }

    @Override
    public C toConfiguration(ComponentSetup context) {
        // Vil godt lave den om til CNC (Hvad det end betyder). Maaske at vi gerne vil bruge invokeExact
        C c;
        try {
            // TODO.. vi bruger ikke context'en lige nu. Men
            c = (C) configurationConstructor.invoke(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(c, context);
        return c;
    }

    @Override
    public PackedBeanDriver<C> with(Wirelet... wirelet) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected ComponentDriver<C> withWirelet(Wirelet w) {
        throw new UnsupportedOperationException();
    }
}
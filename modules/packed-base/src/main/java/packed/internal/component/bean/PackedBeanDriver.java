package packed.internal.component.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.component.BeanConfiguration;
import app.packed.component.BeanDriver;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
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

    final MethodHandle mh;

    public PackedBeanDriver(PackedBeanDriverBinder<?, C> binder, Object binding) {
        super(null);
        this.mh = binder.constructor();
        this.binding = requireNonNull(binding);
        this.isConstant = binder.isConstant();
    }

    @Override
    public Class<?> beanType() {
        if (binding instanceof Class<?> cl) {
            return cl;
        } else if (binding instanceof Factory<?> f) {
            return f.rawType();
        } else {
            return binding.getClass();
        }
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
            c = (C) mh.invoke(context);
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
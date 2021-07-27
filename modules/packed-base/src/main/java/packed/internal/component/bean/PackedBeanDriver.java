package packed.internal.component.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanType;
import app.packed.bean.OldBeanDriver;
import app.packed.component.Wirelet;
import packed.internal.application.BuildSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.RealmSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link OldBeanDriver}. */
public final class PackedBeanDriver<C extends BeanConfiguration> extends PackedComponentDriver<C> implements OldBeanDriver<C> {

    /** The bean type. */
    private final Class<?> beanType;

    /** The bean binder used for creating this driver. */
    final PackedBeanDriverBinder<?, C> binder;

    /** The actual binding. Either a Class, Factory or (generic) instance. */
    final Object binding;

    public PackedBeanDriver(PackedBeanDriverBinder<?, C> binder, Class<?> beanType, Object binding) {
        super(null);
        this.binder = requireNonNull(binder);
        this.beanType = requireNonNull(beanType);
        this.binding = requireNonNull(binding);
    }

    public Class<?> beanType() {
        return beanType;
    }

    public BeanType kind() {
        return binder.kind();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentSetup newComponent(BuildSetup build, RealmSetup realm, LifetimeSetup lifetime, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        return new BeanSetup(build, lifetime, realm, this, parent, wirelets);
    }

    @Override
    public C toConfiguration0(ComponentSetup context) {
        try {
            // TODO.. vi bruger ikke context'en lige nu. Men
            return (C) binder.constructor.invoke(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }
}
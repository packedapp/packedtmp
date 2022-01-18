package app.packed.buildold;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.hooks.usage.BeanOldKind;
import app.packed.component.ComponentConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link OldBeanDriver}. */
public final class PackedBeanDriver<C extends BeanConfiguration<?>> implements OldBeanDriver<C> {

    /** The bean type. */
    private final Class<?> beanType;

    /** The bean binder used for creating this driver. */
    final PackedBeanDriverBinder<?, C> binder;

    /** The actual binding. Either a Class, Factory or (generic) instance. */
    public final Object binding;

    public PackedBeanDriver(PackedBeanDriverBinder<?, C> binder, Class<?> beanType, Object binding) {
        this.binder = requireNonNull(binder);
        this.beanType = requireNonNull(beanType);
        this.binding = requireNonNull(binding);
    }

    public Class<?> beanType() {
        return beanType;
    }

    public BeanOldKind kind() {
        return binder.kind();
    }

    public C toConfiguration0(ComponentSetup context) {
        try {
            // TODO.. vi bruger ikke context'en lige nu. Men
            return (C) binder.constructor.invoke(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }
    
    /** A handle that can access ComponentConfiguration#component. */
    private static final VarHandle VH_COMPONENT_CONFIGURATION_COMPONENT = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(),
            ComponentConfiguration.class, "component", ComponentSetup.class);


    public final C toConfiguration(ComponentSetup cs) {
        C c = toConfiguration0(cs);
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(c, cs);
        return c;
    }
}
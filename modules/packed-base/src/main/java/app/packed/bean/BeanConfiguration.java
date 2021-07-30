package app.packed.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.concurrent.Callable;

import app.packed.base.Key;
import app.packed.component.ComponentConfiguration;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.component.bean.BeanSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

public abstract non-sealed class BeanConfiguration extends ComponentConfiguration {

    /** A handle that can access superclass private ComponentConfiguration#component(). */
    private static final MethodHandle MH_COMPONENT_CONFIGURATION_COMPONENT = MethodHandles.explicitCastArguments(
            LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class, "component", ComponentSetup.class),
            MethodType.methodType(BeanSetup.class, BeanConfiguration.class));

    /** {@return the container setup instance that we are wrapping.} */
    private BeanSetup bean() {
        try {
            return (BeanSetup) MH_COMPONENT_CONFIGURATION_COMPONENT.invokeExact(this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    // Her kan en extension faktisk exporte ting...
    protected <T> ExportedServiceConfiguration<T> exportAsService() {
        return bean().sourceExport();
    }

    /**
     * This method can be overridden to return a subclass of bean mirror.
     * 
     * {@inheritDoc}
     */
    @Override
    protected BeanMirror mirror() {
        throw new UnsupportedOperationException();
    }

    protected void provideAsService() {
        bean().sourceProvide();
    }

    protected void provideAsService(Key<?> key) {
        bean().sourceProvideAs(key);
    }

    protected Optional<Key<?>> sourceProvideAsKey() {
        return bean().sourceProvideAsKey();
    }

    public <T extends Runnable & Callable<String>> T foo() {
        return null;
    }
}

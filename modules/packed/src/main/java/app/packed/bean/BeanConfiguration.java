package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.NamespacePath;
import app.packed.component.ComponentConfiguration;
import packed.internal.bean.PackedBeanHandle;

/**
 * The base configuration class of a single bean.
 */
public non-sealed class BeanConfiguration extends ComponentConfiguration {

    /** The bean handle. */
    final PackedBeanHandle<?> beanHandle;

    /**
     * Create a new bean configuration using the specified handle.
     * 
     * @param handle
     *            the bean handle
     */
    public BeanConfiguration(BeanHandle<?> handle) {
        this.beanHandle = requireNonNull((PackedBeanHandle<?>) handle, "handle is null");
    }

    /**
     * {@return the kind of bean that is being configured.}
     * 
     * @see BeanHandle#beanKind()
     */
    public final Class<?> beanClass() {
        return beanHandle.beanClass();
    }

    /**
     * {@return the kind of bean that is being configured.}
     * 
     * @see BeanHandle#beanKind()
     */
    public final BeanKind beanKind() {
        return beanHandle.bean().beanKind();
    }

    /** {@inheritDoc} */
    @Override
    protected final void checkIsWiring() {
        beanHandle.bean().checkIsActive();
    }

    /** {@return a handle for the configuration of the bean.} */
    protected BeanHandle<?> handle() {
        return beanHandle;
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration named(String name) {
        beanHandle.bean().named(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final NamespacePath path() {
        return beanHandle.bean().path();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return beanHandle.bean().toString();
    }
}

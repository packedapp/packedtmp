package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.NamespacePath;
import app.packed.component.ComponentConfiguration;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.PackedBeanHandle;

/**
 * The base class for the configuration of a bean.
 * <p>
 * {@code BeanConfiguration} is the superclass of the various bean configuration classes available in Packed.
 */
public non-sealed class BeanConfiguration extends ComponentConfiguration {

    /** The internal configuration of the bean. */
    final BeanSetup bean;

    /**
     * Create a new bean configuration using the specified handle.
     * 
     * @param handle
     *            the bean handle
     */
    public BeanConfiguration(BeanHandle<?> handle) {
        this.bean = requireNonNull((PackedBeanHandle<?>) handle, "handle is null").bean();
    }

    /**
     * {@return the kind of bean that is being configured.}
     * 
     * @see BeanHandle#beanKind()
     */
    public final Class<?> beanClass() {
        return bean.beanClass();
    }
    
    /**
     * {@return the kind of bean that is being configured.}
     * 
     * @see BeanHandle#beanKind()
     */
    public final BeanKind beanKind() {
        return bean.beanKind();
    }

    /** {@inheritDoc} */
    @Override
    protected final void checkIsWiring() {
        bean.checkIsActive();
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration named(String name) {
        bean.named(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final NamespacePath path() {
        return bean.path();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return bean.toString();
    }
}

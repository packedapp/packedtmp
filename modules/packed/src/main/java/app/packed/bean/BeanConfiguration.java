package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.NamespacePath;
import app.packed.component.ComponentConfiguration;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.PackedBeanDriver;

/**
 * The base class for the configuration of a bean.
 * <p>
 * {@code BeanConfiguration} is the superclass of the various bean configuration classes available in Packed.
 */
public non-sealed class BeanConfiguration extends ComponentConfiguration {

    /** The internal configuration of the bean. */
    final BeanSetup bean;

    /**
     * Create a new bean configuration using the specified bean driver.
     * 
     * @param driver
     *            the driver of the bean
     * @throws IllegalStateException
     *             if the specified driver has already been used to create a new configuration object
     */
    public BeanConfiguration(BeanDriver<?> driver) {
        PackedBeanDriver<?> d = requireNonNull((PackedBeanDriver<?>) driver, "driver is null");
        this.bean = d.newSetup(this);
    }

    /**
     * {@return the kind of bean that is being configured.}
     * 
     * @see BeanDriver#beanKind()
     */
    public final Class<?> beanClass() {
        return bean.driver.beanClass();
    }
    
    /**
     * {@return the kind of bean that is being configured.}
     * 
     * @see BeanDriver#beanKind()
     */
    public final BeanKind beanKind() {
        return bean.driver.beanKind();
    }

    /** {@inheritDoc} */
    @Override
    protected final void checkIsWiring() {
        bean.checkIsActive();
    }

    /**
     * This method can be overridden to return a subclass of bean mirror.
     * 
     * {@inheritDoc}
     * 
     * @throws IllegalStateException
     *             if the configuration has not been wired yet
     */
    @Override
    protected final BeanMirror mirror() {
        // Jeg taenker det er er
        throw new UnsupportedOperationException();
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

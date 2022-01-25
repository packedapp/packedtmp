package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.NamespacePath;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.ExtensionBeanConfiguration;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.PackedBeanMaker;

/**
 * The base class for the configuration of a bean.
 * <p>
 * {@code BeanConfiguration} is the superclass of the various bean configuration classes available in Packed.
 * 
 */
public non-sealed class BeanConfiguration extends ComponentConfiguration {

    /** The bean we are configuring. */
    private final BeanSetup bean;

    protected BeanConfiguration(BeanMaker<?> maker) {
        PackedBeanMaker<?> pbm = requireNonNull((PackedBeanMaker<?>) maker, "maker is null");
        this.bean = pbm.newSetup(this);
    }

    @Override
    protected final void checkIsWiring() {
        bean.checkIsActive();
    }

    /** {@return the kind of bean that is being configured. } */
    public final BeanKind kind() {
        if (this instanceof ContainerBeanConfiguration) {
            return BeanKind.CONTAINER;
        } else if (this instanceof ExtensionBeanConfiguration) {
            return BeanKind.EXTENSION;
        } else if (this instanceof ManagedBeanConfiguration) {
            return BeanKind.MANAGED;
        } else {
            return BeanKind.UNMANAGED;
        }
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
    protected BeanMirror mirror() {
        // Jeg taenker det er er
        throw new UnsupportedOperationException();
    }

    // Ved ikke praecis hvad den overskriver...

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

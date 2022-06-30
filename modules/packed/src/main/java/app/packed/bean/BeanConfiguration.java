package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.application.ComponentMirror;
import app.packed.base.NamespacePath;
import internal.app.packed.bean.PackedBeanHandler;

/**
 * The base configuration class for a bean.
 */
public class BeanConfiguration {

    /** The bean handle. */
    final PackedBeanHandler<?> beanHandle;

    /**
     * Create a new bean configuration using the specified handle.
     * 
     * @param handle
     *            the bean handle
     */
    public BeanConfiguration(BeanHandler<?> handle) {
        this.beanHandle = requireNonNull((PackedBeanHandler<?>) handle, "handle is null");
    }

    /**
     * {@return the kind of bean that is being configured.}
     * 
     * @see BeanHandler#beanClass()
     */
    public final Class<?> beanClass() {
        return beanHandle.beanClass();
    }

    /**
     * {@return the kind of bean that is being configured.}
     * 
     * @see BeanHandler#beanKind()
     */
    public final BeanKind beanKind() {
        return beanHandle.bean().beanKind();
    }

    /**
     * Checks that the bean is still configurable or throws an {@link IllegalStateException} if not.
     * 
     * @throws IllegalStateException
     *             if the bean is no longer configurable
     */
    protected final void checkIsConfigurable() {
        if (!beanHandle.isConfigurable()) {
            throw new IllegalStateException("The bean is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    protected final void checkIsCurrent() {
        beanHandle.bean().checkIsCurrent();
    }

    /** {@return a handle for the configuration of the bean.} */
    protected BeanHandler<?> handle() {
        return beanHandle;
    }

    /**
     * Sets the name of the bean. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name is
     * case sensitive.
     * <p>
     * If no name is explicitly set on for a bean. A name will be assigned to the bean (at build time) in such a way that it
     * will have a unique name among other sibling components.
     *
     * @param name
     *            the name of the bean
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see ComponentMirror#name()
     */
    public BeanConfiguration named(String name) {
        beanHandle.bean().named(name);
        return this;
    }

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #named(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     * 
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    public final NamespacePath path() {
        return beanHandle.bean().path();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return beanHandle.bean().toString();
    }
}

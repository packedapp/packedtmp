package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.container.User;
import app.packed.framework.NamespacePath;

/** The base configuration class for a bean. */
public class BeanConfiguration {

    /** The bean handle. */
    private final BeanHandle<?> handle;

    /**
     * Create a new bean configuration using the specified handle.
     * 
     * @param handle
     *            the bean handle
     */
    public BeanConfiguration(BeanHandle<?> handle) {
        this.handle = requireNonNull((BeanHandle<?>) handle, "handle is null");
    }

    /** {@return the bean class.} */
    public final Class<?> beanClass() {
        return handle.beanClass();
    }

    /** {@return the kind of bean that is being configured.} */
    public final BeanKind beanKind() {
        return handle.beanKind();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof BeanConfiguration bc && handle == bc.handle;
    }

    /** {@return the bean handle that was used to create this configuration.} */
    protected BeanHandle<?> handle() {
        return handle;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return handle.hashCode();
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
     * @throws RuntimeException
     *             if there is another bean with the same name in the container. Or if the container has a child container
     *             with the same name.
     */
    public BeanConfiguration named(String name) {
        handle.named(name);
        return this;
    }

    /** {@return the owner of the bean.} */
    public final User owner() {
        return handle.owner();
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
    // I'm not sure we want to expose midt build? IDK
    // If only for user... Maybe is nice for debugging
    public final NamespacePath path() {
        return handle.path();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return handle.toString();
    }
}

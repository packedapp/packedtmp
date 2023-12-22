package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import app.packed.application.OldApplicationPath;
import app.packed.component.ComponentConfiguration;
import app.packed.container.Operative;
import app.packed.context.Context;
import app.packed.util.Key;
import internal.app.packed.bean.PackedBeanHandle;
import sandbox.extension.bean.BeanHandle;

/** The configuration of a bean, typically returned from the bean's installation site. */
public non-sealed class BeanConfiguration extends ComponentConfiguration implements BeanLocalAccessor {

    /** The bean handle. */
    private final PackedBeanHandle<?> handle;

    /**
     * Create a new bean configuration using the specified handle.
     *
     * @param handle
     *            the bean handle
     */
    public BeanConfiguration(BeanHandle<?> handle) {
        super(handle);
        this.handle = (PackedBeanHandle<?>) requireNonNull(handle, "handle is null");
    }

    /**
     * Allows to install multiple beans with the same bean class in the same container.
     * <p>
     * Beans that have {@code void} bean class are automatically multi class.
     *
     * @return this configuration
     * @throws UnsupportedOperationException
     *             if called on a bean with void bean class
     */
    public BeanConfiguration allowMultiClass() {
        handle.allowMultiClass();
        return this;
    }

    /** {@return the owner of the bean.} */
    public final Operative author() {
        return handle.author();
    }

    /** {@return the bean class.} */
    public final Class<?> beanClass() {
        return handle.beanClass();
    }

    /** {@return the kind of bean that is being configured.} */
    public final BeanKind beanKind() {
        return handle.beanKind();
    }

    /**
     * {@return set of contexts this bean operates within.}
     * <p>
     * The returned set will not only contexts that are specific to the bean. But also any contexts that are available all
     * throughout the bean's container.
     * <p>
     * This method is primarily used for informational purposes.
     */
    public final Set<Class<? extends Context<?>>> contexts() {
        // Need to filter hidden
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof BeanConfiguration bc && handle == bc.handle;
    }

    /** {@return the bean handle that was used to create this configuration.} */
    protected final BeanHandle<?> handle() {
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

    public <K> BeanConfiguration overrideService(Class<K> key, K constant) {
        // Future Functionality:

        // overrideService(key, Provider) -> Needs some use cases
        // And when is it generated? When the bean is instantiated?
        // And is is per bean or use site?

        // overrideService(key, Op) ->
        // The issue is here that Op needs to be resolved.
        // Maybe Op includes a service that have already been overridden.
        // Not saying its impossible. But currently we do not support
        // Adding operations dynamically after the bean has been scanned.
        return overrideService(Key.of(key), constant);
    }

    /**
     * Overrides a previously resolved service with the specified key.
     *
     * @param <K>
     *            type of the constant
     * @param key
     *            the key of the service
     * @param constant
     *            the constant to bind the service to
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the bean does not have binding that are resolved as a service with the specified key
     */
    public <K> BeanConfiguration overrideService(Key<K> key, K constant) {
        handle.overrideService(key, constant);
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
    // I'm not sure we want to expose midt build? IDK
    // If only for user... Maybe is nice for debugging
    public final OldApplicationPath path() {
        return handle.path();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return handle.toString();
    }
}

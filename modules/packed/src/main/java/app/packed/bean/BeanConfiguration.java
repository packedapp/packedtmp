package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import app.packed.component.Authority;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.context.Context;
import app.packed.operation.OperationConfiguration;
import app.packed.util.Key;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanHandle;
import sandbox.extension.bean.BeanHandle;

/**
 * The configuration of a bean, typically returned from the bean's installation site or via
 * {@link app.packed.container.ContainerConfiguration#beans()}.
 */
public non-sealed class BeanConfiguration implements ComponentConfiguration , BeanLocalAccessor {

    /** The bean handle. We don't store BeanSetup directly because it is not generified */
    private final PackedBeanHandle<?> handle;

    public BeanConfiguration() {
        // Will fail if the bean configuration is not initialized from within the framework
        this.handle = new PackedBeanHandle<>(BeanSetup.initFromBeanConfiguration(this));
    }

    /**
     * Create a new bean configuration using the specified handle.
     *
     * @param handle
     *            the bean handle
     */
    public BeanConfiguration(BeanHandle<?> handle) {
        this.handle = (PackedBeanHandle<?>) requireNonNull(handle, "handle is null");
        this.handle.bean().initConfiguration(this);
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

    public <K> void addCodeGenerator(Class<K> key, Supplier<? extends K> supplier) {
        addCodeGenerator(Key.of(key), supplier);
    }

    /**
     * Registers a code generating supplier whose supplied value can be consumed by a variable annotated with
     * {@link CodeGenerated} at runtime for any bean in the underlying container.
     * <p>
     * Internally this mechanisms uses
     *
     * <p>
     * The value if the code generator is not available outside of the underlying container.
     *
     * @param <K>
     *            the type of value the supplier produces
     * @param bean
     *            the bean to bind the supplier to
     * @param key
     *            the type of key used together with {@link CodeGenerated}
     * @param supplier
     *            the supplier generating the value
     *
     * @throws IllegalArgumentException
     *             if the specified bean is not owned by this extension. Or if the specified bean is not part of the same
     *             container as this extension. Or if the specified bean does not have an injection site matching the
     *             specified key.
     * @throws IllegalStateException
     *             if a supplier has already been registered for the specified key in the same container, or if the
     *             extension is no longer configurable.
     * @see CodeGenerated
     * @see BindableVariable#bindGeneratedConstant(Supplier)
     */
    public <K> void addCodeGenerator(Key<K> key, Supplier<? extends K> supplier) {
        throw new UnsupportedOperationException();
    }

    /** {@return the owner of the bean.} */
    // Declared by???
    public final Authority author() {
        return handle.owner();
    }

    /** {@return the bean class.} */
    public final Class<?> beanClass() {
        return handle.beanClass();
    }

    /** {@return the kind of bean that is being configured.} */
    public final BeanKind beanKind() {
        return handle.beanKind();
    }

    /** {@return the path of the component} */
    @Override
    public final ComponentPath componentPath() {
        return handle.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration componentTag(String... tags) {
        handle.componentTags(tags);
        return this;
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

    /**
     * {@return a stream of any bean instance factories this bean has.}
     * <p>
     * Static beans will always return an empty stream.
     */
    public final Stream<BeanFactoryConfiguration> factories() {
        return operations(BeanFactoryConfiguration.class);
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

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurable() {
        return handle.isConfigurable();
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

    /** {@return configurations for all operations defined by this bean.} */
    public final Stream<? extends OperationConfiguration> operations() {
        return handle.bean().operations.all.stream().map(m -> m.configuration).filter(e -> e != null);
    }

    /** {@return configurations for all operations defined by this bean.} */
    @SuppressWarnings("unchecked")
    public final <T extends OperationConfiguration> Stream<T> operations(Class<? super T> operationType) {
        return (Stream<T>) operations().filter(e -> operationType.isInstance(e));
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return handle.toString();
    }
}

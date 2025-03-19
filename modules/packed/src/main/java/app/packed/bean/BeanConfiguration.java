package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import app.packed.binding.Key;
import app.packed.build.action.BuildActionable;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentRealm;
import app.packed.context.Context;
import app.packed.extension.Extension;
import app.packed.operation.OperationConfiguration;

/**
 * The configuration of a bean.
 * <p>
 * A bean configuration is typically returned from the bean's installation site. For example,
 * {@link app.packed.extension.BaseExtension#install(Class)}. It can also, for example, be obtained via
 * {@link app.packed.container.ContainerConfiguration#beans()}.
 */
public non-sealed class BeanConfiguration extends ComponentConfiguration implements BeanLocal.Accessor {

    /** The bean handle. */
    private final BeanHandle<?> handle;

    /**
     * Create a new bean configuration using the specified handle.
     *
     * @param handle
     *            the bean's handle
     */
    public BeanConfiguration(BeanHandle<?> handle) {
        this.handle = requireNonNull(handle, "handle is null");
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
    @BuildActionable("bean.allowMultiClass")
    public BeanConfiguration allowMultiClass() {
        checkIsConfigurable();
        handle.allowMultiClass();
        return this;
    }

    /**
     * {@return a set of the contexts that available to the bean}
     * <p>
     * These contexts can only be injected doing the initialization of the bean. Either through a factory method or using
     * {@link Inject} or {@link app.packed.bean.lifecycle.Initialize}.
     */
    public final Set<? extends Context<?>> availableContexts() {
        throw new UnsupportedOperationException();
    }

    /** {@return the bean class.} */
    public final Class<?> beanClass() {
        return handle.beanClass();
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
    /** {@return the kind of bean that is being configured.} */
    public final BeanKind beanKind() {
        return handle.beanKind();
    }

    @BuildActionable("bean.addCodeGenerator")
    public <K> void bindCodeGenerator(Class<K> key, Supplier<? extends K> supplier) {
        checkIsConfigurable();
        bindCodeGenerator(Key.of(key), supplier);
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
     *             If the specified bean does not have an injection site matching the specified key.
     * @throws IllegalStateException
     *             if a supplier has already been registered for the specified key in the same container, or if the bean is
     *             no longer configurable.
     * @see ComputedConstant
     * @see BindableVariable#bindGeneratedConstant(Supplier)
     */
    @BuildActionable("bean.addCodeGenerator")
    // ComputedService instead??? buildConstant??? IDK
    // Hmm, bliver vi noedt til at sige noget om hvornaar den bliver kaldt???
    // Den bliver jo tidligst kaldt som en del af build processen

    // bindCodeGenerator <--- Will install it as a normal service....
    // was bindComputedConstant
    // bind(Supplier) <-- every time we create the bean
    // bindSuppler(BuildTime, CodegenTime, Lazy, PerUsage); // Multithreaded???
    // Lazy_Per_ApplicationInstance, ...
    public <K> void bindCodeGenerator(Key<K> key, Supplier<? extends K> supplier) {
        checkIsConfigurable();
        handle.bindCodeGenerator(key, supplier);
    }

    public <K> BeanConfiguration bindServiceInstance(Class<K> key, K instance) {
        // Future Functionality:

        // overrideService(key, Op) ->
        // The issue is here that Op needs to be resolved.
        // Maybe Op includes a service that have already been overridden.
        // Not saying its impossible. But currently we do not support
        // Adding operations dynamically after the bean has been scanned.
        return bindServiceInstance(Key.of(key), instance);
    }

    // provideTo??
    // provideAs vs provideI

    // bindinstance

    /**
     * Binds a (bean) service from the specified key to the specified instance.
     * <p>
     * If a service has already been specified on the bean with the same key it is overwritten.
     * <p>
     * IMPORTANT: The specified instance will not be treated as a bean, but merely as an instance
     *
     * @param <K>
     *            type of the instance
     * @param key
     *            the key of the service
     * @param instance
     *            the instance to bind the service to
     * @return this configuration
     */
    public <K> BeanConfiguration bindServiceInstance(Key<K> key, K instance) {
        checkIsConfigurable();
        handle.bindServiceInstance(key, instance);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @BuildActionable("component.addTags") // Hmm or bean.addTags
    public BeanConfiguration tag(String... tags) {
        checkIsConfigurable();
        handle.componentTag(tags);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<String> tags() {
        return handle.componentTags();
    }

    /**
     * {@return set of contexts this bean operates within.}
     * <p>
     * The returned set will not only contexts that are specific to the bean. But also any contexts that are available all
     * throughout the bean's container.
     * <p>
     * This method is primarily used for informational purposes.
     */
    //Is this still used
    public final Set<Class<? extends Context<?>>> contexts() {
        // Need to filter hidden
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected final BeanHandle<?> handle() {
        return handle;
    }

    public final Class<? extends Extension<?>> installedBy() {
        return handle.bean.installedBy.extensionType;
    }

    // Outside of the framework I think we can only test on ComponentPath, that may be fine
    public final boolean isInSameContainer(BeanConfiguration other) {
        return handle.bean.container == other.handle.bean.container;
    }

    /**
     * Sets the name of the bean. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name is
     * case sensitive.
     * <p>
     * If no name is explicitly set on for a bean. A name will be assigned to the bean (at build time) in such a way that it
     * will have a unique name among other sibling components.
     * <p>
     * Beans belonging to extensions will always have the simple name of the extension followed by "#" prefixed to the name
     * of the bean.
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
    @BuildActionable("bean.named")
    public BeanConfiguration named(String name) {
        checkIsConfigurable();
        handle.named(name);
        return this;
    }

    // Called if bean is renamed
    // Returning String is currentName
    String onBeanRename(BiConsumer<String, String> oldNewName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the configuration for the specified operation type.
     *
     * @param <T>
     *            the type of the operation configuration
     * @param operationType
     *            the class type of the operation configuration
     * @return the configuration for the specified operation type
     * @throws IllegalStateException
     *             if the number of configurations found is not exactly 1
     */
    final <T extends OperationConfiguration> T operation(Class<T> operationType) {
        List<T> list = operations(operationType).toList();
        if (list.size() != 1) {
            throw new IllegalStateException("Expected exactly 1 configuration for operation type: " + operationType.getName() + ", but found: " + list.size());
        }
        return list.get(0);
    }

    // Named instead //operation(ScheduledOperationConfiguration, "foo");
    final <T extends OperationConfiguration> T operation(Class<T> operationType, String operationName) {
        return operation(operationType);
    }


    /** {@return a stream of operation configurations for all the operations that currently defined on this bean} */
    public final Stream<? extends OperationConfiguration> operations() {
        return handle.bean.operations.stream().map(m -> m.handle().configuration()).filter(e -> e != null);
    }

    /**
     * {@return a stream of operation configurations for all the operations that currently defined on this bean}
     *
     * @param <T>
     *            the type of operation configuration
     * @param operationType
     *            the of operations configurations the stream should include
     */
    @SuppressWarnings("unchecked")
    public final <T extends OperationConfiguration> Stream<T> operations(Class<T> operationType) {
        return (Stream<T>) operations().filter(e -> operationType.isInstance(e));
    }

    /** {@return the owner of the bean.} */
    // Declared by???
    public final ComponentRealm owner() {
        return handle.owner();
    }

    // Ideen er at have alle de keys that are resolved as services
    protected Set<Key<?>> requestedServiceKeys() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return handle.toString();
    }
}

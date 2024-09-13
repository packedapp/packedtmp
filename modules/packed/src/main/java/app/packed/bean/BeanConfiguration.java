package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import app.packed.bean.BeanLocal.Accessor;
import app.packed.build.BuildLocal;
import app.packed.build.action.BuildActionable;
import app.packed.component.Authority;
import app.packed.component.ComponentConfiguration;
import app.packed.context.Context;
import app.packed.operation.OperationConfiguration;
import app.packed.util.Key;
import internal.app.packed.bean.PackedBeanHandle;
import internal.app.packed.bean.PackedBeanInstaller;

/**
 * The configuration of a bean.
 * <p>
 * A bean configuration is typically returned from the bean's installation site. For example,
 * {@link app.packed.extension.BaseExtension#install(Class)}. It can also, for example, be obtained via
 * {@link app.packed.container.ContainerConfiguration#beans()}.
 */
public non-sealed class BeanConfiguration extends ComponentConfiguration implements Accessor {

    /** The bean handle. We don't store BeanSetup directly because BeanHandle contains a lot of useful logic. */
    private final PackedBeanHandle<?> handle;

    /**
     * Create a new bean configuration using the specified installer.
     *
     * @param installer
     *            the bean installer
     */
    public BeanConfiguration(BeanTemplate.Installer installer) {
        requireNonNull(installer, "installer is null");
        this.handle = ((PackedBeanInstaller) installer).initializeBeanConfiguration();
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
        handle.allowMultiClass();
        return this;
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

    // Alternativt tager vi ikke en bean. Men en container som er implicit
    // Det betyder nu ogsaa at CodeGenerated er for hele containeren og ikke bare en bean.
    // Maaske supportere begge ting?
    // Det eneste jeg kunne forstille mig at man ikke ville container wide var hvis man havde en bean
    // per X. Men taenker men saa har et arrays

    // Hmm, vi faar ogsaa foerst fejl senere saa. Men siden det er en extension. Er det nok ikke det store problem i praksis
    // Evt. Bliver den bare aldrig kaldt.... Det er fint

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
    // It is so simple maybe add it to bean configuration
    // Or maybe a subset space...
    // BeanConfiguration overrideService(Class<? extends Annotation>> subset, Key, Supplier);

    /** {@return the kind of bean that is being configured.} */
    public final BeanKind beanKind() {
        return handle.beanKind();
    }

    // Outside of the framework I think we can only test on ComponentPath, that may be fine
    public final boolean isInSameContainer(BeanConfiguration other) {
        return handle.bean().container == other.handle.bean().container;
    }

    @BuildActionable("bean.addCodeGenerator")
    // bindCodeGenerator
    public <K> void bindCodeGenerator(Class<K> key, Supplier<? extends K> supplier) {
        bindCodeGenerator(Key.of(key), supplier);
    }

    // Called if bean is renamed
    // Returning String is currentName
    public String onBeanRename(BiConsumer<String, String> oldNewName) {
        throw new UnsupportedOperationException();
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
    public <K> void bindCodeGenerator(Key<K> key, Supplier<? extends K> supplier) {
        handle.addComputedConstant(key, supplier);
    }

    // Kommer an på typen af local hvordan vi slaar den op.
    // Fx taenker jeg vi supportere OperationLocal
    <K> void bindComputedConstant(Class<K> key, BuildLocal<?, ? extends K> supplier) {
        throw new UnsupportedOperationException();
    }

    public <K> BeanConfiguration bindInstance(Class<K> key, K constant) {
        // Future Functionality:

        // overrideService(key, Provider) -> Needs some use cases
        // And when is it generated? When the bean is instantiated?
        // And is is per bean or use site?

        // overrideService(key, Op) ->
        // The issue is here that Op needs to be resolved.
        // Maybe Op includes a service that have already been overridden.
        // Not saying its impossible. But currently we do not support
        // Adding operations dynamically after the bean has been scanned.
        return bindInstance(Key.of(key), constant);
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
    @BuildActionable("bean.overrideService")
    // On Op it is just bind
    public <K> BeanConfiguration bindInstance(Key<K> key, K constant) {
        handle.overrideService(key, constant);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    protected final BeanHandle<?> handle() {
        return handle;
    }

    /** {@inheritDoc} */
    @Override
    @BuildActionable("component.addTags") // Hmm or bean.addTags
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

    /**
     * {@return a stream of any bean instance factories this bean has.}
     * <p>
     * Static beans will always return an empty stream.
     */
    public final Stream<BeanFactoryConfiguration> factories() {
        return operations(BeanFactoryConfiguration.class);
    }

    public final Optional<BeanFactoryConfiguration> factory() {
        return operations(BeanFactoryConfiguration.class).findAny();
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
        handle.named(name);
        return this;
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
    public final <T extends OperationConfiguration> T operation(Class<T> operationType) {
        List<T> list = operations(operationType).toList();
        if (list.size() != 1) {
            throw new IllegalStateException("Expected exactly 1 configuration for operation type: " + operationType.getName() + ", but found: " + list.size());
        }
        return list.get(0);
    }

    /** {@return configurations for all operations defined by this bean.} */
    public final Stream<? extends OperationConfiguration> operations() {
        return handle.bean().operations.stream().map(m -> m.configuration).filter(e -> e != null);
    }

    /** {@return configurations for all operations defined by this bean.} */
    @SuppressWarnings("unchecked")
    public final <T extends OperationConfiguration> Stream<T> operations(Class<T> operationType) {
        return (Stream<T>) operations().filter(e -> operationType.isInstance(e));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return handle.toString();
    }
}

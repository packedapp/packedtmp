package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.Key;
import app.packed.bean.hooks.BeanField;
import app.packed.bean.hooks.BeanMethod;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.service.Provide;
import app.packed.inject.service.ServiceLocator;
import app.packed.operation.OperationPack;
import app.packed.operation.dependency.DependencyProvider;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.ExtensionBeanSetup;
import packed.internal.bean.PackedBeanHandleBuilder;
import packed.internal.bean.hooks.BeanMemberDependencyNode;
import packed.internal.bean.hooks.FieldHelper;
import packed.internal.bean.hooks.MethodHelper;
import packed.internal.bean.hooks.PackedBeanMember;
import packed.internal.bean.hooks.PackedDependencyProvider;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.inject.DependencyNode;
import packed.internal.inject.service.runtime.AbstractServiceLocator;

/**
 * An extension for creating new beans.
 */
public class BeanExtension extends Extension<BeanExtension> {

    /** The container we are installing beans into. */
    final ContainerSetup container;

    /**
     * Create a new bean extension.
     * 
     * @param configuration
     *            an extension configuration object
     */
    /* package-private */ BeanExtension(ExtensionConfiguration configuration) {
        this.container = ((ExtensionSetup) configuration).container;
    }

    /** {@inheritDoc} */
    @Override
    protected void hookOnBeanField(BeanField field) {
        Key<?> key = Key.convertField(field.field());
        boolean constant = field.field().getAnnotation(Provide.class).constant();

        BeanSetup bean = ((PackedBeanMember) field).bean;
        FieldHelper fh = new FieldHelper(field, field.newRawOperation().handle(), constant, key);
        DependencyNode node = new BeanMemberDependencyNode(bean, fh, fh.createProviders());
        field.newOperationSetter();

        bean.parent.injectionManager.addConsumer(node);
    }

    /** {@inheritDoc} */
    @Override
    protected void hookOnBeanMethod(BeanMethod method) {
        Key<?> key = Key.convertMethodReturnType(method.method());
        boolean constant = method.method().getAnnotation(Provide.class).constant();

        BeanSetup bean = ((PackedBeanMember) method).bean;
        MethodHelper fh = new MethodHelper(method, method.newRawOperation().handle(), constant, key);
        DependencyNode node = new BeanMemberDependencyNode(bean, fh, fh.createProviders());

        // Er ikke sikker paa vi har en runtime bean...
        method.newOperation(null);

        bean.parent.injectionManager.addConsumer(node);
    }

    /** {@inheritDoc} */
    @Override
    protected void hookOnBeanDependencyProvider(DependencyProvider provider) {
        // We only have a hook for OperationPack
        BeanSetup bean = ((PackedDependencyProvider) provider).operation().bean;

        // OperationPacks can only be used with extension beans
        if (bean instanceof ExtensionBeanSetup e) {
            e.provideOperationPack(provider);
        } else {
            provider.failWith(OperationPack.class.getSimpleName() + " can only be injected into extension beans installed using "
                    + BeanExtensionPoint.class.getSimpleName());
        }
    }

    /**
     * Installs a bean that will use the specified {@link Class} to instantiate a single instance of the bean when the
     * application is initialized.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param implementation
     *            the type of bean to install
     * @return the configuration of the bean
     * @see BaseAssembly#install(Class)
     */
    public <T> ContainerBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = PackedBeanHandleBuilder.ofClass(null, BeanKind.CONTAINER, container, implementation).build();
        return new ContainerBeanConfiguration<>(handle);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Factory)
     */
    public <T> ContainerBeanConfiguration<T> install(Factory<T> factory) {
        BeanHandle<T> handle = PackedBeanHandleBuilder.ofFactory(null, BeanKind.CONTAINER, container, factory).build();
        return new ContainerBeanConfiguration<>(handle);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this container will have have component as its
     * parent.
     *
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    public <T> ContainerBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = PackedBeanHandleBuilder.ofInstance(null, BeanKind.CONTAINER, container, instance).build();
        return new ContainerBeanConfiguration<>(handle);
    }

    /** {@inheritDoc} */
    @Override
    protected BeanExtensionMirror mirror() {
        return mirrorInitialize(new BeanExtensionMirror());
    }

    /** {@inheritDoc} */
    @Override
    protected void onAssemblyClose() {
        container.injectionManager.resolve();
    }

    /**
     * Provides every service from the specified locator.
     * 
     * @param locator
     *            the locator to provide services from
     * @throws IllegalArgumentException
     *             if the specified locator is not implemented by Packed
     */
    public void provideAll(ServiceLocator locator) {
        requireNonNull(locator, "locator is null");
        if (!(locator instanceof AbstractServiceLocator l)) {
            throw new IllegalArgumentException("Custom implementations of " + ServiceLocator.class.getSimpleName()
                    + " are currently not supported, locator type = " + locator.getClass().getName());
        }
        checkConfigurable();
        container.injectionManager.provideAll(l);
    }

    public <T> ProvidableBeanConfiguration<T> providePrototype(Class<T> implementation) {
        BeanHandle<T> handle = PackedBeanHandleBuilder.ofClass(null, BeanKind.UNMANAGED, container, implementation).build();
        ProvidableBeanConfiguration<T> sbc = new ProvidableBeanConfiguration<T>(handle);
        return sbc.provide();
    }

    public <T> ProvidableBeanConfiguration<T> providePrototype(Factory<T> factory) {
        BeanHandle<T> handle = PackedBeanHandleBuilder.ofFactory(null, BeanKind.UNMANAGED, container, factory).build();
        ProvidableBeanConfiguration<T> sbc = new ProvidableBeanConfiguration<T>(handle);
        return sbc.provide();
    }
}

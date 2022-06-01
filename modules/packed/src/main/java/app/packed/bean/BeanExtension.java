package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import app.packed.inject.Provide;
import app.packed.inject.service.ServiceLocator;
import app.packed.inject.service.ServiceTransformer;
import app.packed.operation.OperationPack;
import app.packed.operation.dependency.DependencyProvider;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.ExtensionBeanSetup;
import packed.internal.bean.PackedBeanHandleBuilder;
import packed.internal.bean.hooks.PackedBeanField;
import packed.internal.bean.hooks.PackedBeanMethod;
import packed.internal.bean.inject.BeanMemberDependencyNode;
import packed.internal.bean.inject.FieldHelper;
import packed.internal.bean.inject.MethodHelper;
import packed.internal.bean.inject.PackedDependencyProvider;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.inject.DependencyNode;
import packed.internal.inject.service.runtime.AbstractServiceLocator;

/**
 * An extension for installing beans into a container.
 */
public class BeanExtension extends Extension<BeanExtension> {

    /** The container we are installing beans into. */
    final ContainerSetup container;

    /** Create a new bean extension. */
    /* package-private */ BeanExtension(/* hidden */ ExtensionSetup setup) {
        this.container = setup.container;
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
    public <T> ProvideableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandler<T> handle = PackedBeanHandleBuilder.ofClass(null, BeanKind.CONTAINER, container, implementation).build();
        return new ProvideableBeanConfiguration<>(handle);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Factory)
     */
    public <T> ProvideableBeanConfiguration<T> install(Factory<T> factory) {
        BeanHandler<T> handle = PackedBeanHandleBuilder.ofFactory(null, BeanKind.CONTAINER, container, factory).build();
        return new ProvideableBeanConfiguration<>(handle);
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
    public <T> ProvideableBeanConfiguration<T> installInstance(T instance) {
        BeanHandler<T> handle = PackedBeanHandleBuilder.ofInstance(null, BeanKind.CONTAINER, container, instance).build();
        return new ProvideableBeanConfiguration<>(handle);
    }

    void installNested(Object classOrFactory) {

    }

    @Override
    protected BeanScanner newBeanScanner() {
        return new BeanScanner() {

            /** {@inheritDoc} */
            @Override
            public void onBeanVariable(BeanVariable v) {
                DependencyProvider provider = v.dp();
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

            /** {@inheritDoc} */
            @Override
            public void onBeanField(BeanField field) {
                // readKey

                Key<?> key = Key.convertField(field.field());
                boolean constant = field.field().getAnnotation(Provide.class).constant();

                BeanSetup bean = ((PackedBeanField) field).bean;
                FieldHelper fh = new FieldHelper(field, ((PackedBeanField) field).newVarHandle(), constant, key);
                DependencyNode node = new BeanMemberDependencyNode(bean, fh, fh.createProviders());
                field.newSetOperation(null);

                bean.parent.injectionManager.addConsumer(node);
            }

            /** {@inheritDoc} */
            @Override
            public void onBeanMethod(BeanMethod method) {
                Key<?> key = Key.convertMethodReturnType(method.method());
                boolean constant = method.method().getAnnotation(Provide.class).constant();

                BeanSetup bean = ((PackedBeanMethod) method).bean;
                MethodHelper fh = new MethodHelper(method, ((PackedBeanMethod) method).newMethodHandle(), constant, key);
                DependencyNode node = new BeanMemberDependencyNode(bean, fh, fh.createProviders());

                // Er ikke sikker paa vi har en runtime bean...
                // method.newOperation(null);

                bean.parent.injectionManager.addConsumer(node);
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    protected BeanExtensionMirror newExtensionMirror() {
        return new BeanExtensionMirror();
    }

    @Override
    protected BeanExtensionPoint newExtensionPoint() {
        return new BeanExtensionPoint();
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
        checkIsConfigurable();
        container.injectionManager.provideAll(l);
    }

    public void provideAll(ServiceLocator locator, Consumer<ServiceTransformer> transformer) {
        // ST.contract throws UOE
    }

    public <T> ProvideableBeanConfiguration<T> providePrototype(Class<T> implementation) {
        BeanHandler<T> handle = PackedBeanHandleBuilder.ofClass(null, BeanKind.UNMANAGED, container, implementation).build();
        ProvideableBeanConfiguration<T> sbc = new ProvideableBeanConfiguration<T>(handle);
        return sbc.provide();
    }

    public <T> ProvideableBeanConfiguration<T> providePrototype(Factory<T> factory) {
        BeanHandler<T> handle = PackedBeanHandleBuilder.ofFactory(null, BeanKind.UNMANAGED, container, factory).build();
        ProvideableBeanConfiguration<T> sbc = new ProvideableBeanConfiguration<T>(handle);
        return sbc.provide();
    }
}

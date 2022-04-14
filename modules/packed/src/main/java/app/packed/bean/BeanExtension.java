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
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceLocator;
import packed.internal.bean.BeanOperationSetup;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.PackedBeanHandle;
import packed.internal.bean.PackedBeanHandleBuilder;
import packed.internal.bean.hooks.BeanMemberDependencyNode;
import packed.internal.bean.hooks.BeanScanner;
import packed.internal.bean.hooks.FieldHelper;
import packed.internal.bean.hooks.HookedBeanField;
import packed.internal.bean.hooks.HookedBeanMethod;
import packed.internal.bean.hooks.MethodHelper;
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

    @Override
    protected void hookOnBeanField(BeanField field) {
        BeanScanner f = ((HookedBeanField) field).scanner;
        BeanSetup bean = f.bean;
        Key<?> key = Key.convertField(field.field());
        boolean constant = field.field().getAnnotation(Provide.class).constant();
        FieldHelper fh = new FieldHelper(field, field.rawOperation().handle(), constant, key);
        DependencyNode node = new BeanMemberDependencyNode(bean, fh, fh.createProviders());

        BeanOperationSetup os = new BeanOperationSetup(bean, ServiceExtension.class);
        bean.addOperation(os);
        // os.mirrorSupplier = supplier;

        bean.parent.injectionManager.addConsumer(node);
    }

    @Override
    protected void hookOnBeanMethod(BeanMethod method) {
        // new Exception().printStackTrace();
        BeanScanner f = ((HookedBeanMethod) method).scanner;
        BeanSetup bean = f.bean;
        Key<?> key = Key.convertMethodReturnType(method.method());
        boolean constant = method.method().getAnnotation(Provide.class).constant();
        MethodHelper fh = new MethodHelper(method, method.rawOperation().handle(), constant, key);
        DependencyNode node = new BeanMemberDependencyNode(bean, fh, fh.createProviders());

        BeanOperationSetup os = new BeanOperationSetup(bean, ServiceExtension.class);
        bean.addOperation(os);
        // os.mirrorSupplier = supplier;

        bean.parent.injectionManager.addConsumer(node);
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
        PackedBeanHandle<T> driver = PackedBeanHandleBuilder.ofClass(BeanKind.CONTAINER, container, BeanExtension.class, container.assembly.realm(), implementation).build();
        return new ContainerBeanConfiguration<>(driver);
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
        // Med mindre vi laver en User->Extension, skal vi jo have noget a.la. UserOrExtension.realm();
        PackedBeanHandle<T> handle = PackedBeanHandleBuilder.ofFactory(BeanKind.CONTAINER, container, BeanExtension.class, container.assembly.realm(), factory).build();
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
        PackedBeanHandle<T> handle = PackedBeanHandleBuilder.ofInstance(BeanKind.CONTAINER, container, BeanExtension.class, container.assembly.realm(), instance).build();
        return new ContainerBeanConfiguration<>(handle);
    }

    /** {@inheritDoc} */
    @Override
    protected BeanExtensionMirror mirror() {
        return mirrorInitialize(new BeanExtensionMirror(tree()));
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
        PackedBeanHandle<T> handle = PackedBeanHandleBuilder.ofClass(BeanKind.UNMANAGED, container, BeanExtension.class, container.assembly.realm(), implementation).build();
        ProvidableBeanConfiguration<T> sbc = new ProvidableBeanConfiguration<T>(handle);
        return sbc.provide();
    }

    public <T> ProvidableBeanConfiguration<T> providePrototype(Factory<T> factory) {
        PackedBeanHandle<T> handle = PackedBeanHandleBuilder.ofFactory(BeanKind.UNMANAGED, container, BeanExtension.class, container.assembly.realm(), factory).build();
        ProvidableBeanConfiguration<T> sbc = new ProvidableBeanConfiguration<T>(handle);
        return sbc.provide();
    }
}

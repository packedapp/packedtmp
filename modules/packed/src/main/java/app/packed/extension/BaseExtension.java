package app.packed.extension;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.Inject;
import app.packed.bean.OnInitialize;
import app.packed.bean.OnStart;
import app.packed.bean.OnStop;
import app.packed.container.BaseAssembly;
import app.packed.extension.BaseExtensionPoint.BeanInstaller;
import app.packed.lifetime.RunState;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.LifetimeOperation;
import internal.app.packed.operation.OperationSetup;

/**
 * An extension that defines the foundational APIs managing beans, containers and applications.
 * <p>
 * This extension is automatically used by every container.
 */
public class BaseExtension extends FrameworkExtension<BaseExtension> {

    /** The internal configuration of the extension. */
    final ExtensionSetup extensionSetup = ExtensionSetup.crack(this);

    /** Create a new bean extension. */
    BaseExtension() {}

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
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).install(implementation);
        return new ProvideableBeanConfiguration<>(handle);
    }

    /**
     * Installs a component that will use the specified {@link Op} to instantiate the component instance.
     * 
     * @param op
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Op)
     */
    public <T> ProvideableBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).install(op);
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
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).installInstance(instance);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Class<T> implementation) {
        BeanHandle<T> handle = newInstaller(BeanKind.LAZY).install(implementation);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Op<T> op) {
        BeanHandle<T> handle = newInstaller(BeanKind.LAZY).install(op);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    /**
     * Installs a new {@link BeanKind#STATIC static} bean.
     * 
     * @param implementation
     *            the static bean class
     * @return a configuration for the bean
     * 
     * @see BeanKind#STATIC
     * @see BeanSourceKind#CLASS
     */
    public BeanConfiguration installStatic(Class<?> implementation) {
        BeanHandle<?> handle = newInstaller(BeanKind.STATIC).install(implementation);
        return new BeanConfiguration(handle);
    }

    /**
     * @see BeanKind#CONTAINER
     * @see BeanSourceKind#CLASS
     * @see BeanHandle.InstallOption#multi()
     */
    public <T> ProvideableBeanConfiguration<T> multiInstall(Class<T> implementation) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).multi().install(implementation);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> multiInstall(Op<T> op) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).multi().install(op);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallInstance(T instance) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).multi().installInstance(instance);
        return new ProvideableBeanConfiguration<>(handle);
    }

    // Skriv usecases naeste gang. Taenker over det hver gang
    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Class<T> implementation) {
        BeanHandle<T> handle = newInstaller(BeanKind.LAZY).multi().install(implementation);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Op<T> op) {
        BeanHandle<T> handle = newInstaller(BeanKind.LAZY).multi().install(op);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    /**
     * Creates a new BeanIntrospector for handling annotations managed by BeanExtension.
     * 
     * @see Inject
     * @see OnInitialize
     * @see OnStart
     * @see OnStop
     */
    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void onField(OnField field) {
                if (field.annotations().isAnnotationPresent(Inject.class)) {

                }
            }

            @Override
            public void onMethod(OnMethod method) {
                AnnotationReader ar = method.annotations();

                OperationTemplate temp = OperationTemplate.defaults().withReturnType(method.operationType().returnType());
                //(PackedInvocationType) operation.invocationType.withReturnType(type.returnType());
                
                if (ar.isAnnotationPresent(OnInitialize.class)) {
                    @SuppressWarnings("unused")
                    OnInitialize oi = ar.readRequired(OnInitialize.class);
                    OperationSetup os = OperationSetup.crack(method.newOperation(temp));
                    os.bean.operationsLifetime.add(new LifetimeOperation(RunState.INITIALIZING, os));
                }

                if (ar.isAnnotationPresent(OnStart.class)) {
                    @SuppressWarnings("unused")
                    OnStart oi = ar.readRequired(OnStart.class);
                    OperationSetup os = OperationSetup.crack(method.newOperation(temp));
                    os.bean.operationsLifetime.add(new LifetimeOperation(RunState.STARTING, os));
                }

                if (ar.isAnnotationPresent(OnStop.class)) {
                    @SuppressWarnings("unused")
                    OnStop oi = ar.readRequired(OnStop.class);
                    OperationSetup os = OperationSetup.crack(method.newOperation(temp));
                    os.bean.operationsLifetime.add(new LifetimeOperation(RunState.STOPPING, os));
                }

                if (ar.isAnnotationPresent(Inject.class)) {
                    OperationSetup.crack(method.newOperation(temp));
                }
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    protected BaseExtensionMirror newExtensionMirror() {
        return new BaseExtensionMirror();
    }

    /** {@inheritDoc} */
    @Override
    protected BaseExtensionPoint newExtensionPoint() {
        return new BaseExtensionPoint();
    }

    BeanInstaller newInstaller(BeanKind kind) {
        return new PackedBeanInstaller(extensionSetup, kind, null);
    }
}

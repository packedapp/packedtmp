package app.packed.extension;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.BindableVariable;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.Inject;
import app.packed.bean.OnInitialize;
import app.packed.bean.OnStart;
import app.packed.bean.OnStop;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.container.Assembly;
import app.packed.container.BaseAssembly;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtensionPoint.BeanInstaller;
import app.packed.extension.BaseExtensionPoint.CodeGenerated;
import app.packed.extension.BaseExtensionPoint.ContainerInstaller;
import app.packed.lifetime.sandbox.ManagedLifetimeController;
import app.packed.operation.Op;
import app.packed.operation.Op1;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationTemplate.InvocationArgument;
import app.packed.service.ProvideableBeanConfiguration;
import app.packed.service.ServiceLocator;
import internal.app.packed.bean.BeanLocal;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.bean.PackedBindableVariable;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;

/**
 * An extension that defines the foundational APIs for managing beans, containers and applications.
 * <p>
 * Every container will automatically have this extension added. And every extension automatically has a direct
 * dependency on this extension.
 */
public class BaseExtension extends FrameworkExtension<BaseExtension> {

    static final BeanLocal<Map<Key<?>, BindableVariable>> CODEGEN = BeanLocal.of();

    /** Variables that used together with {@link CodeGenerated}. */
    private final Map<CodeGeneratorKey, BindableVariable> codegenVariables = new HashMap<>();

    boolean isLinking;

    /** Create a new base extension. */
    BaseExtension() {}

    <K> void addCodeGenerated(BeanSetup bean, Key<K> key, Supplier<? extends K> supplier) {
        // BindableVariable bv = CODEGEN.get(bean).get(key);

        BindableVariable prev = codegenVariables.get(new CodeGeneratorKey(bean, key));

        if (prev == null) {
            throw new IllegalArgumentException("The specified bean must have an injection site that uses @" + CodeGenerated.class.getSimpleName() + " " + key);
        } else if (prev.isBound()) {
            throw new IllegalStateException("A supplier has previously been provided for key [key = " + key + ", bean = " + bean + "]");
        }

        prev.bindToGeneratedConstant(supplier);
    }

    final void embed(Assembly assembly) {
        /// MHT til hooks. Saa tror jeg faktisk at man tager de bean hooks
        // der er paa den assembly der definere dem

        // Men der er helt klart noget arbejde der
        throw new UnsupportedOperationException();
    }

    /**
     * Installs a bean of the specified type. A single instance of the specified class will be instantiated when the
     * container is initialized.
     * 
     * @param implementation
     *            the type of bean to install
     * @return the configuration of the bean
     * @see BaseAssembly#install(Class)
     */
    public <T> ProvideableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.CONTAINER).install(implementation);
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
        BeanHandle<T> handle = newBeanInstaller(BeanKind.CONTAINER).install(op);
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
        BeanHandle<T> handle = newBeanInstaller(BeanKind.CONTAINER).installInstance(instance);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Class<T> implementation) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.LAZY).install(implementation);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Op<T> op) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.LAZY).install(op);
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
        BeanHandle<?> handle = newBeanInstaller(BeanKind.STATIC).install(implementation);
        return new BeanConfiguration(handle);
    }

    /**
     * Creates a new child container by linking the specified assembly.
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     */
    public void link(Assembly assembly, Wirelet... wirelets) {
        newContainerInstaller().link(assembly, wirelets);
    }

    /**
     * @see BeanKind#CONTAINER
     * @see BeanSourceKind#CLASS
     * @see BeanHandle.InstallOption#multi()
     */
    public <T> ProvideableBeanConfiguration<T> multiInstall(Class<T> implementation) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.CONTAINER).multi().install(implementation);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> multiInstall(Op<T> op) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.CONTAINER).multi().install(op);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallInstance(T instance) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.CONTAINER).multi().installInstance(instance);
        return new ProvideableBeanConfiguration<>(handle);
    }

    // Skriv usecases naeste gang. Taenker over det hver gang
    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Class<T> implementation) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.LAZY).multi().install(implementation);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Op<T> op) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.LAZY).multi().install(op);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    BeanInstaller newBeanInstaller(BeanKind kind) {
        return new PackedBeanInstaller(extension, kind, null);
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

            /** Handles {@link Inject}. */
            @Override
            public void hookOnAnnotatedField(Set<Class<? extends Annotation>> hooks, OperationalField field) {
                if (field.annotations().isAnnotationPresent(Inject.class)) {

                    // handle.specializeMirror(() -> new BeanLifecycleOperationMirror());
                }
            }

            /** Handles {@link Inject}, {@link OnInitialize}, {@link OnStart} and {@link OnStop}. */
            @Override
            public void hookOnAnnotatedMethod(Annotation annotation, OperationalMethod method) {
                BeanSetup bean = BeanSetup.crack(method);
                OperationTemplate temp = OperationTemplate.defaults().withReturnType(method.operationType().returnType());

                if (annotation instanceof Inject) {
                    OperationHandle handle = method.newOperation(temp);
                    bean.lifecycle.addInitialize(handle, null);
                } else if (annotation instanceof OnInitialize oi) {
                    OperationHandle handle = method.newOperation(temp);
                    bean.lifecycle.addInitialize(handle, oi.ordering());
                } else if (annotation instanceof OnStart oi) {
                    OperationHandle handle = method.newOperation(temp);
                    bean.lifecycle.addStart(handle, oi.ordering());
                } else if (annotation instanceof OnStop oi) {
                    OperationHandle handle = method.newOperation(temp);
                    bean.lifecycle.addStop(handle, oi.ordering());
                } else {
                    super.hookOnAnnotatedMethod(annotation, method);
                }
            }

            /** Handles {@link FromGuest}, {@link InvocationArgument} and {@link CodeGenerated}. */
            @Override
            public void hookOnAnnotatedVariable(Annotation hook, BindableVariable v) {
                if (hook instanceof FromGuest) {
                    Variable va = v.variable();
                    if (va.getRawType().equals(String.class)) {
                        v.bindTo(new Op1<@InvocationArgument ApplicationInitializationContext, String>(a -> a.name()) {});
                    } else if (va.getRawType().equals(ManagedLifetimeController.class)) {
                        v.bindTo(new Op1<@InvocationArgument ApplicationInitializationContext, ManagedLifetimeController>(a -> a.cr.runtime) {});
                    } else if (va.getRawType().equals(ServiceLocator.class)) {
                        v.bindTo(new Op1<@InvocationArgument ApplicationInitializationContext, ServiceLocator>(a -> a.serviceLocator()) {});
                    } else {
                        throw new UnsupportedOperationException("va " + va.getRawType());
                    }
                } else if (hook instanceof InvocationArgument ia) {
                    int index = ia.index();
                    Class<?> cl = v.variable().getRawType();
                    List<Class<?>> l = v.availableInvocationArguments();
                    if (cl != l.get(index)) {
                        throw new UnsupportedOperationException();
                    }

                    v.bindToInvocationArgument(index);
                } else if (hook instanceof CodeGenerated cg) {
                    BeanSetup bean = ((PackedBindableVariable) v).operation.bean;
                    if (beanOwner().isApplication()) {
                        throw new BeanInstallationException("@" + CodeGenerated.class.getSimpleName() + " can only be used by extensions");
                    }

                    // Create the key
                    Key<?> key = v.variableToKey();

                    // CODEGEN.get(this).putIfAbsent(key, v);
                    BindableVariable bv = codegenVariables.putIfAbsent(new CodeGeneratorKey(bean, key), v);
                    if (bv != null) {
                        failWith(key + " Can only be injected once for bean ");
                    }
                } else {
                    super.hookOnAnnotatedVariable(hook, v);
                }
            }

            @Override
            public void hookOnVariableType(Class<?> hook, BindableBaseVariable v) {
                if (hook == ExtensionContext.class) {
                    if (v.availableInvocationArguments().isEmpty() || v.availableInvocationArguments().get(0) != ExtensionContext.class) {
                        // throw new Error(v.availableInvocationArguments().toString());
                    }
                    v.bindToInvocationArgument(0);
                } else {
                    super.hookOnVariableType(hook, v);
                }
            }
        };
    }

    ContainerInstaller newContainerInstaller() {
        return new PackedContainerInstaller(extension.container);
    }

    /** {@inheritDoc} */
    @Override
    protected BaseExtensionPoint newExtensionPoint() {
        return new BaseExtensionPoint();
    }

    @Override
    protected void onAssemblyClose() {
        // 3 ways to form trees
        // Application, Assembly, Lifetime

        boolean isLinking = parent().map(e -> e.isLinking).orElse(false);
        if (isLinking) {
            // navigator().forEachInAssembly()->
        }

        // process child extensions first
        super.onAssemblyClose();

        // A lifetime root lets order some dependencies
        if (isLifetimeRoot()) {
            extension.container.lifetime.orderDependencies();
        }
    }

    record CodeGeneratorKey(BeanSetup bean, Key<?> key) {}
}

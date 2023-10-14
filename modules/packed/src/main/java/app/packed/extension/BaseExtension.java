package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.application.ApplicationMirror;
import app.packed.application.BuildException;
import app.packed.application.DeploymentMirror;
import app.packed.bean.BeanClassTransformer;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanMirror;
import app.packed.bean.Inject;
import app.packed.bean.ManagedBeanRequiredException;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLocal;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtensionPoint.CodeGenerated;
import app.packed.extension.BeanElement.BeanField;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.lifetime.Main;
import app.packed.lifetime.OnInitialize;
import app.packed.lifetime.OnStart;
import app.packed.lifetime.OnStop;
import app.packed.operation.Op;
import app.packed.operation.Op1;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationMirror;
import app.packed.operation.Provider;
import app.packed.service.Export;
import app.packed.service.Provide;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceOutgoingTransformer;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.Key;
import app.packed.util.Variable;
import internal.app.packed.bean.BeanLifecycleOrder;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanHandleBuilder;
import internal.app.packed.bean.PackedBindableWrappedVariable;
import internal.app.packed.binding.BindingResolution.FromOperationResult;
import internal.app.packed.container.NonRootContainerBuilder;
import internal.app.packed.entrypoint.OldEntryPointSetup;
import internal.app.packed.entrypoint.OldEntryPointSetup.MainThreadOfControl;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.PackedServiceLocator;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.MethodHandleUtil;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.bean.BeanTemplate;
import sandbox.extension.container.ContainerCarrierService;
import sandbox.extension.container.ContainerHandle;
import sandbox.extension.container.ContainerTemplate;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;
import sandbox.lifetime.external.LifecycleController;

/**
 * An extension that defines the foundational APIs for managing beans, services, containers and applications.
 * <p>
 * Every container automatically uses this extension. And every extension automatically has a direct dependency on this
 * extension.
 * <p>
 * All methods on this class deals with beans Table area [bean,container,service] prefix desciption
 *
 *
 * <p>
 * This extension does not define an {@link ExtensionExtension extension mirror}. Instead all relevant methods are
 * placed directly on {@link app.packed.bean.BeanMirror}, {@link app.packed.container.ContainerMirror} and
 * {@link app.packed.application.ApplicationMirror}.
 *
 * @see app.packed.container.BaseAssembly#base()
 * @see BaseWirelets
 */

// Bean
//// install

// Container
//// link

// Service
//// export
//// require
//// provide
//// transform/rewrite??? depends on 1 or two interfaces

public class BaseExtension extends FrameworkExtension<BaseExtension> {

    /** A bean local for variables that use {@link app.packed.extension.BaseExtensionPoint.CodeGenerated}. */
    private static final BeanLocal<Map<Key<?>, BindableVariable>> CODEGEN = BeanLocal.of(() -> new HashMap<>());

    // We use an initial value for now, because we share FromLinks and the boolean fields
    // But right now we only have a single field
    static final ContainerLocal<FromLinks> FROM_LINKS = ContainerLocal.ofContainer(FromLinks::new);

    final ArrayList<BindableVariable> varsToResolve = new ArrayList<>();

    /** All your base are belong to us. */
    BaseExtension() {}

    <K> void addCodeGenerated(BeanSetup bean, Key<K> key, Supplier<? extends K> supplier) {
        Map<Key<?>, BindableVariable> m = bean.container.application.locals.get(CODEGEN, bean);
        BindableVariable var = m.get(key);
        if (var == null) {
            throw new IllegalArgumentException("The specified bean must have an injection site that uses @" + CodeGenerated.class.getSimpleName() + " " + key
                    + ". Available " + m.keySet());
        } else if (var.isBound()) {
            throw new IllegalStateException("A supplier has previously been provided for key [key = " + key + ", bean = " + bean + "]");
        }

        var.bindComputedConstant(supplier);
    }

    // One of 3 models...
    // Fails on other exports
    // Ignores other exports
    // interacts with other exports in some way
    /**
     * Exports all container services and any services that have been explicitly anchored via of anchoring methods.
     * <p>
     *
     * <ul>
     * <li><b>Service already exported.</b> The service that have already been exported (under any key) are always
     * ignored.</li>
     * <li><b>Key already exported.</b>A service has already been exported under the specified key.
     * <li><b>Are requirements.</b> Services that come from parent containers are always ignored.</li>
     * <li><b>Not part of service contract.</b> If a service contract has set. Only services for whose key is part of the
     * contract is exported.</li>
     * </ul>
     * <p>
     * This method can be invoked more than once. But use cases for this are limited.
     */
    // Altsaa tror mere vi er ude efter noget a.la. exported.services = internal.services
    // Saa maske smide ISE hvis der allerede er exporteret services. Det betyder naesten ogsaa
    // at @Export ikke er supporteret
    // ect have exportAll(boolean ignoreExplicitExports) (Otherwise fails)

    // All provided services are automatically exported
    public void exportAll() {
        // Tror vi aendre den til streng service solve...
        // Og saa tager vi bare alle services() og exportere

        // Add exportAll(Predicate); //Maybe some exportAll(Consumer<ExportedConfg>)
        // exportAllAs(Function<?, Key>

        // Export all entries except foo which should be export as Boo
        // exportAll(Predicate) <- takes key or service configuration???

        // export all _services_.. Also those that are already exported as something else???
        // I should think not... Det er er en service vel... SelectedAll.keys().export()...
        checkIsConfigurable();

        extension.container.sm.exportAll = true;
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
    public <T> ServiceableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = install0(BeanKind.CONTAINER.template()).install(implementation);
        return new ServiceableBeanConfiguration<>(handle);
    }

    /**
     * Installs a component that will use the specified {@link Op} to instantiate the component instance.
     *
     * @param op
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Op)
     */
    public <T> ServiceableBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<T> handle = install0(BeanKind.CONTAINER.template()).install(op);
        return new ServiceableBeanConfiguration<>(handle);
    }

    private PackedBeanHandleBuilder install0(BeanTemplate template) {
        return new PackedBeanHandleBuilder(extension, extension.container.assembly, template);
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
    public <T> ServiceableBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = install0(BeanKind.CONTAINER.template()).installInstance(instance);
        return new ServiceableBeanConfiguration<>(handle);
    }

    public <T> ServiceableBeanConfiguration<T> installLazy(Class<T> implementation) {
        BeanHandle<T> handle = install0(BeanKind.LAZY.template()).install(implementation);
        return new ServiceableBeanConfiguration<>(handle); // Providable???
    }

    public <T> ServiceableBeanConfiguration<T> installLazy(Op<T> op) {
        BeanHandle<T> handle = install0(BeanKind.LAZY.template()).install(op);
        return new ServiceableBeanConfiguration<>(handle); // Providable???
    }

    public <T> ServiceableBeanConfiguration<T> installPrototype(Class<T> implementation) {
        BeanHandle<T> handle = install0(BeanKind.UNMANAGED.template()).install(implementation);
        return new ServiceableBeanConfiguration<>(handle);
    }

    public <T> ServiceableBeanConfiguration<T> installPrototype(Op<T> op) {
        BeanHandle<T> handle = install0(BeanKind.UNMANAGED.template()).install(op);
        return new ServiceableBeanConfiguration<>(handle);
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
        BeanHandle<?> handle = install0(BeanKind.STATIC.template()).install(implementation);
        return new BeanConfiguration(handle);
    }

    /**
     * Installs an exported service locator.
     *
     * @see BaseExtensionPoint#EXPORTED_SERVICE_LOCATOR
     */
    void lifetimeExportServiceLocator() {
        // Create a new bean that holds the ServiceLocator to export
        // will fail if installed multiple times
        BeanHandle<PackedServiceLocator> h = newBeanBuilderSelf(BeanKind.CONTAINER.template()).install(PackedServiceLocator.class)
                .exportAs(ServiceLocator.class);

        // PackedServiceLocator needs a Map<Key, MethodHandle> which is created in the code generation phase
        addCodeGenerated(BeanSetup.crack(h), new Key<Map<Key<?>, MethodHandle>>() {}, () -> extension.container.sm.exportedServices());

        // Alternative, If we do not use it for anything else
        newBeanBuilderSelf(BeanKind.CONTAINER.template()).installIfAbsent(PackedServiceLocator.class, bh -> {
            bh.exportAs(ServiceLocator.class);
            addCodeGenerated(BeanSetup.crack(bh), new Key<Map<Key<?>, MethodHandle>>() {}, () -> extension.container.sm.exportedServices());
        });
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
        link0().build(assembly, wirelets);
    }

    /**
     * Creates a new container that strongly linked to the lifetime of this container.
     *
     * @param wirelets
     *            optional wirelets
     * @return configuration for the new container
     */
    public ContainerConfiguration link(Wirelet... wirelets) {
        ContainerHandle handle = link0().build(wirelets);
        return new ContainerConfiguration(handle);
    }

    /** {@return a new container builder used for linking.} */
    private ContainerHandle.Builder link0() {
        return NonRootContainerBuilder.of(ContainerTemplate.DEFAULT, BaseExtension.class, extension.container.application, extension.container);
    }

    /**
     * Returns a special bean installer that can install beans with BaseExtension as both the owner and installer.
     *
     * @param template
     *            a template for the bean
     * @return a bean installer
     */
    private BeanHandle.Builder newBeanBuilderSelf(BeanTemplate template) {
        return new PackedBeanHandleBuilder(extension, extension, template);
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

            /** A template for bean lifecycle operations. */
            private static final OperationTemplate BEAN_LIFECYCLE_TEMPLATE = OperationTemplate.defaults().returnIgnore();

            private OperationHandle checkNotStaticBean(Class<? extends Annotation> annotationType, BeanMethod method) {
                if (beanKind() == BeanKind.STATIC) {
                    throw new ManagedBeanRequiredException(annotationType + " is not supported for static beans");
                }
                // Maybe lifecycle members cannot be static at all
                return method.newOperation(BEAN_LIFECYCLE_TEMPLATE);
            }

            /** Handles {@link Inject}. */
            @Override
            public void hookOnAnnotatedField(Annotation hook, BeanField field) {
                if (hook instanceof Inject) {
                    // checkNotStatic
                    // Det er jo inject service!???
                 //   field.newBindableVariable().unwrap();
                    // OperationHandle handle = field.newSetOperation(null) .newOperation(temp);
                    // bean.lifecycle.addInitialize(handle, null);
                    throw new UnsupportedOperationException();
                } else if (hook instanceof Provide) {
                    Key<?> key = field.toKey();

                    if (!Modifier.isStatic(field.modifiers())) {
                        if (beanKind() != BeanKind.CONTAINER) {
                            throw new BuildException("Not okay)");
                        }
                    }

                    OperationSetup operation = OperationSetup.crack(field.newGetOperation(OperationTemplate.defaults()));
                    extension.container.sm.provide(key, operation, new FromOperationResult(operation));
                } else {
                    super.hookOnAnnotatedField(hook, field);
                }
            }

            /** Handles {@link Inject}, {@link OnInitialize}, {@link OnStart} and {@link OnStop}. */
            @Override
            public void hookOnAnnotatedMethod(Annotation annotation, BeanMethod method) {
                BeanSetup bean = bean();

                if (annotation instanceof Inject) {
                    OperationHandle handle = checkNotStaticBean(Inject.class, method);
                    bean.addLifecycleOperation(BeanLifecycleOrder.INJECT, handle);
                } else if (annotation instanceof OnInitialize oi) {
                    OperationHandle handle = checkNotStaticBean(OnInitialize.class, method);
                    bean.addLifecycleOperation(BeanLifecycleOrder.fromInitialize(oi.order()), handle);
                } else if (annotation instanceof OnStart oi) {
                    OperationHandle handle = checkNotStaticBean(OnStart.class, method);
                    bean.addLifecycleOperation(BeanLifecycleOrder.fromStarting(oi.order()), handle);
                } else if (annotation instanceof OnStop oi) {
                    OperationHandle handle = checkNotStaticBean(OnStop.class, method);
                    bean.addLifecycleOperation(BeanLifecycleOrder.fromStopping(oi.order()), handle);
                } else if (annotation instanceof Provide) {
                    OperationTemplate temp2 = OperationTemplate.defaults().returnType(method.operationType().returnRawType());
                    if (!Modifier.isStatic(method.modifiers())) {
                        if (beanKind() != BeanKind.CONTAINER) {
                            throw new BeanInstallationException("Not okay)");
                        }
                    }
                    OperationSetup operation = OperationSetup.crack(method.newOperation(temp2));
                    bean.container.sm.provide(method.toKey(), operation, new FromOperationResult(operation));
                } else if (annotation instanceof Export) {
                    OperationTemplate temp2 = OperationTemplate.defaults().returnType(method.operationType().returnRawType());

                    if (!Modifier.isStatic(method.modifiers())) {
                        if (beanKind() != BeanKind.CONTAINER) {
                            throw new BeanInstallationException("Not okay)");
                        }
                    }
                    OperationSetup operation = OperationSetup.crack(method.newOperation(temp2));
                    bean.container.sm.export(method.toKey(), operation);
                } else if (annotation instanceof Main) {
                    if (!isInApplicationLifetime()) {
                        throw new BeanInstallationException("Must be in the application lifetime to use @" + Main.class.getSimpleName());
                    }

                    bean.container.lifetime.entryPoints.takeOver(BaseExtension.this, BaseExtension.class);

                    bean.container.lifetime.entryPoints.entryPoint = new OldEntryPointSetup();

                    OperationTemplate temp = OperationTemplate.defaults().returnType(method.operationType().returnRawType());
                    OperationHandle os = method.newOperation(temp);
                    // os.specializeMirror(() -> new EntryPointMirror(index));

                    MainThreadOfControl mc = bean.container.lifetime.entryPoints.entryPoint.mainThread();
                    runOnCodegen(() -> mc.generatedMethodHandle = os.generateMethodHandle());
                } else {
                    super.hookOnAnnotatedMethod(annotation, method);
                }
            }

            /** Handles {@link ContainerGuest}, {@link InvocationArgument} and {@link CodeGenerated}. */
            @Override
            public void hookOnAnnotatedVariable(Annotation annotation, BindableVariable v) {
                if (annotation instanceof ContainerCarrierService) {
                    Variable va = v.variable();
                    if (va.rawType().equals(String.class)) {
                        // Burde vel vaere en generics BeanInvocationContext her???
                        v.bindOp(new Op1<ApplicationLaunchContext, String>(a -> a.name()) {});
                    } else if (va.rawType().equals(LifecycleController.class)) {
                        v.bindOp(new Op1<ApplicationLaunchContext, LifecycleController>(a -> a.runner.runtime) {});
                    } else if (va.rawType().equals(ServiceLocator.class)) {
                        v.bindOp(new Op1<ApplicationLaunchContext, ServiceLocator>(a -> a.serviceLocator()) {});
                    } else {
                        throw new UnsupportedOperationException("va " + va.rawType());
                    }
                } else if (annotation instanceof CodeGenerated cg) {
                    if (beanAuthor().isApplication()) {
                        throw new BeanInstallationException("@" + CodeGenerated.class.getSimpleName() + " can only be used by extensions");
                    }
                    // Create the key
                    Key<?> key = v.toKey();

                    // We currently only allow code generated services to be injected in one place
                    BindableVariable bv = CODEGEN.get(this).putIfAbsent(key, v);
                    if (bv != null) {
                        failWith(key + " Can only be injected once for bean ");
                    }
                    varsToResolve.add(v);
                } else {
                    super.hookOnAnnotatedVariable(annotation, v);
                }
            }

            @Override
            public void hookOnVariableType(Class<?> hook, UnwrappedBindableVariable binding) {
                OperationSetup operation = ((PackedBindableWrappedVariable) binding).v.operation;

                if (ApplicationLaunchContext.class.isAssignableFrom(hook)) {
                    binding.bindContext(ApplicationLaunchContext.class);
                } else if (hook == ExtensionContext.class) {
                    if (beanAuthor().isApplication()) {
                        binding.failWith(hook.getSimpleName() + " can only be injected into extensions");
                    }
//                    if (binding.availableInvocationArguments().isEmpty() || binding.availableInvocationArguments().get(0) != ExtensionContext.class) {
//                        // throw new Error(v.availableInvocationArguments().toString());
//                    }
                    binding.bindContext(ExtensionContext.class);
                } else if (hook == DeploymentMirror.class) {
                    binding.bindConstant(operation.bean.container.application.deployment.mirror());
                } else if (hook == ApplicationMirror.class) {
                    binding.bindConstant(operation.bean.container.application.mirror());
                } else if (hook == ContainerMirror.class) {
                    binding.bindConstant(operation.bean.container.mirror());
                } else if (hook == AssemblyMirror.class) {
                    binding.bindConstant(operation.bean.container.assembly.mirror());
                } else if (hook == BeanMirror.class) {
                    binding.bindConstant(operation.bean.mirror());
                } else if (hook == OperationMirror.class) {
                    binding.bindConstant(operation.mirror());
                } else {
                    // will always fail
                    binding.checkAssignableTo(ExtensionContext.class, DeploymentMirror.class, ApplicationMirror.class, ContainerMirror.class,
                            AssemblyMirror.class, BeanMirror.class, OperationMirror.class);
                }
            }
        };
    }

    /** {@return a mirror for this extension.} */
    @Override
    protected BaseExtensionMirror newExtensionMirror() {
        return new BaseExtensionMirror(extension.container);
    }

    /** {@inheritDoc} */
    @Override
    protected BaseExtensionPoint newExtensionPoint() {
        return new BaseExtensionPoint();
    }

    /**
     * {@inheritDoc}
     * <p>
     * BaseExtension is always the last extension to be closed. As it is the only extension that has
     * {@link internal.app.packed.container.ExtensionModel#orderingDepth()} 0.
     */
    @Override
    protected void onAssemblyClose() {
        // close child extensions first
        super.onAssemblyClose();

        for (BindableVariable v : varsToResolve) {
            if (!v.isBound()) {
                throw new InternalExtensionException(v.toKey() + " not bound for bean ");
            }
        }

        // A lifetime root lets order some dependencies
        if (isLifetimeRoot()) {
            extension.container.lifetime.orderDependencies();
        }
    }

    <T> OperationConfiguration provide(Class<T> key, Provider<? extends T> provider) {
        return provide(Key.of(key), provider);
    }

    <T> OperationConfiguration provide(Key<T> key, Provider<? extends T> provider) {
        throw new UnsupportedOperationException();
    }

    /**
     * Provides every service from the specified service locator.
     *
     * @param locator
     *            the service locator to provide services from
     * @throws KeyAlreadyInUseException
     *             if the service locator provides any keys that are already in use
     */
    public Set<Key<?>> provideAll(ServiceLocator locator) {
        requireNonNull(locator, "locator is null");
        checkIsConfigurable();
        Map<Key<?>, MethodHandle> result = new HashMap<>();
        if (locator instanceof PackedServiceLocator psl) {
            result = CollectionUtil.copyOf(psl.entries(), e -> e.bindTo(psl.context()));
        } else {
            result = CollectionUtil.copyOf(locator.toProviderMap(), p -> MethodHandleUtil.PROVIDER_GET.bindTo(p));
        }
        // I think we will insert a functional bean that provides all the services
        extension.container.sm.provideAll(result);
        return result.keySet(); // can probably return something more clever?
    }

    /**
     * @param <T>
     *            the type of the provided service
     * @param key
     *            the key for which to provide the constant for
     * @param constant
     *            the constant to provide
     * @return a configuration representing the operation
     */
    <T> OperationConfiguration provideConstant(Class<T> key, T constant) {
        return provideConstant(Key.of(key), constant);
    }

    <T> OperationConfiguration provideConstant(Key<T> key, T constant) {
        // Nah skaber den forvirring? Nej det syntes det er rart
        // at have muligheden for ikke at scanne
        throw new UnsupportedOperationException();
    }

    // requires bliver automatisk anchoret...
    // anchorAllChildExports-> requireAllChildExports();
    public void requires(Class<?>... keys) {
        requires(Key.ofAll(keys));
    }

    /**
     * Explicitly adds the specified key to the list of required services. There are typically two situations in where
     * explicitly adding required services can be useful:
     * <p>
     * First, services that are cannot be specified at build time. But is needed later... Is mainly useful when we the
     * services to. For example, importAll() that injector might not a service itself. But other that make use of the
     * injector might.
     *
     *
     * <p>
     * Second, for manual service requirement, although it is often preferable to use contracts here
     * <p>
     * In any but the simplest of cases, contracts are useful
     *
     * @param keys
     *            the key(s) to add
     */
    public void requires(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkIsConfigurable();
        throw new UnsupportedOperationException();
    }

    // Think we need installPrototype (Which will fail if not provided or exported)
    // providePrototype would then be installPrototype().provide() // not ideal
    // Men taenker vi internt typisk arbejde op i mod implementering. Dog ikke altid
    // providePerRequest <-- every time the service is requested
    // Also these beans, can typically just be composites??? Nah

    public void requiresOptionally(Class<?>... keys) {
        requiresOptionally(Key.ofAll(keys));
    }

    /**
     * Adds the specified key to the list of optional services.
     * <p>
     * If a key is added optionally and the same key is later added as a normal (mandatory) requirement either explicitly
     * via # {@link #serviceRequire(Key...)} or implicitly via, for example, a constructor dependency. The key will be
     * removed from the list of optional services and only be listed as a required key.
     *
     * @param keys
     *            the key(s) to add
     */
    // How does this work with child services...
    // They will be consumed
    public void requiresOptionally(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkIsConfigurable();
        throw new UnsupportedOperationException();
    }

    // transformAllBeans() <-- includes extension beans... (Must be open)
    public Runnable transformAllBeans(Consumer<? super BeanClassTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    // All beans that are installed with the assembly
    /**
     * <p>
     * If there are multiple all bean transformers active at the same type. They will be invoked in the order they where
     * registered. The first one registered will be run first
     *
     * @param transformer
     * @return A runnable that be can run after which the transformer will no longer be applied when installing beans.
     */
    // Also a version with BeanClass?? , Class<?>... beanClasses (
    public Runnable transformBeans(Consumer<? super BeanClassTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    /**
     * All beans. the consumer is invoked once on every bean that is installed with
     * <p>
     * If there are any all bean transformers. They will be invoked before this
     *
     * @param transformer
     *            the bean transformer
     */
    public void transformNextBean(Consumer<? super BeanClassTransformer> transformer) {}

    static class FromLinks {
        boolean exportServices;
    }
}

/**
 * An extension that deals with the service functionality of a container.
 * <p>
 * This extension provides the following functionality:
 *
 * is extension provides functionality for exposing and consuming services.
 *
 *
 */
//Functionality for
//* Explicitly requiring services: require, requiOpt & Manual Requirements Management
//* Exporting services: export, exportAll

//// Was
// Functionality for
// * Explicitly requiring services: require, requiOpt & Manual Requirements Management
// * Exporting services: export, exportAll
// * Providing components or injectors (provideAll)
// * Manual Injection

// Har ikke behov for delete fra ServiceLocator

// Future potential functionality
/// Contracts
/// Security for public injector.... Maaske skal man explicit lave en public injector???
/// Transient requirements Management (automatic require unresolved services from children)
/// Integration pits
// MHT til Manuel Requirements Management
// (Hmm, lugter vi noget profile?? Nahh, folk maa extende BaseAssembly og vaelge det..
// Hmm saa auto instantiere vi jo injector extensionen
//// Det man gerne vil kunne sige er at hvis InjectorExtensionen er aktiveret. Saa skal man
// altid bruge Manual Requirements
// contracts bliver installeret direkte paa ContainerConfiguration

// Profile virker ikke her. Fordi det er ikke noget man dynamisk vil switche on an off..
// Maybe have an Container.onExtensionActivation(Extension e) <- man kan overskrive....
// Eller @ContainerStuff(onActivation = FooActivator.class) -> ForActivator extends ContainerController

// Taenker den kun bliver aktiveret hvis vi har en factory med mindste 1 unresolved dependency....
// D.v.s. install(Class c) -> aktivere denne extension, hvis der er unresolved dependencies...
// Ellers selvfoelgelig hvis man bruger provide/@Provides\
//final void embed(Assembly assembly) {
///// MHT til hooks. Saa tror jeg faktisk at man tager de bean hooks
//// der er paa den assembly der definere dem
//
//// Men der er helt klart noget arbejde der
//throw new UnsupportedOperationException();
//}

class ZServiceSandbox {

    public void applicationLink(Assembly assembly, Wirelet... wirelets) {
        // Syntes den er maerkelig hvis vi havde ApplicationWirelet.NEW_APPLICATION
        // Den her er klarere
        // linkNewContainerBuilder().build(assembly, wirelets);
    }

    // Validates the outward facing contract
    public void checkContract(Consumer<? super ServiceContract> validator) {}

    // Det der er ved validator ServiceContractChecker er at man kan faa lidt mere context med
    // checkContract(Service(c->c.checkExact(sc));// ContractChecker.exact(sc));
    public void checkContractExact(ServiceContract sc) {}

    public void exportsTransform(Consumer<? super ServiceOutgoingTransformer> s) {

    }

    public void provideAll(ServiceLocator locator, Consumer<Object> transformer) {
        // ST.contract throws UOE
    }

    public void resolveFirst(Key<?> key) {}

    /**
     * Performs a final transformation of any exported service.
     *
     * This method can perform any final adjustments of services before they are made available to any parent container.
     * <p>
     * The transformation takes place xxxx
     *
     * @param transformer
     *            transforms the exported services
     */
}
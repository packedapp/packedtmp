package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.application.BuildException;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.Inject;
import app.packed.bean.OnInitialize;
import app.packed.bean.OnStart;
import app.packed.bean.OnStop;
import app.packed.bindings.BindableVariable;
import app.packed.bindings.BindableWrappedVariable;
import app.packed.bindings.Key;
import app.packed.bindings.Variable;
import app.packed.container.Assembly;
import app.packed.container.ContainerGuest;
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
import app.packed.service.Export;
import app.packed.service.Provide;
import app.packed.service.ProvideableBeanConfiguration;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceLocator;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.bean.PackedBeanLocal;
import internal.app.packed.binding.BindingResolution.FromOperation;
import internal.app.packed.binding.PackedBindableVariable;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;
import internal.app.packed.operation.OperationSetup;

/**
 * An extension that defines the foundational APIs for managing beans, services, containers and applications.
 * <p>
 * Every container automatically uses this extension. And every extension automatically has a direct dependency on this
 * extension.
 * <p>
 * This extension does not define an {@link ExtensionExtension extension mirror}. Instead all relevant methods are
 * placed directly on {@link app.packed.bean.BeanMirror}, {@link app.packed.container.ContainerMirror} and
 * {@link app.packed.application.ApplicationMirror}.
 *
 * @see BaseExtensionMirror
 */

// Bean
//// install

// Container
//// link

// Service
//// export
//// require
//// provide
public class BaseExtension extends FrameworkExtension<BaseExtension> {

    static final PackedBeanLocal<Map<Key<?>, BindableVariable>> CODEGEN = PackedBeanLocal.of();

    /** Variables that used together with {@link CodeGenerated}. */
    private final Map<CodeGeneratorKey, BindableVariable> codegenVariables = new HashMap<>();

    boolean isLinking;

    /** Create a new base extension. */
    BaseExtension() {}

    <K> void addCodeGenerated(BeanSetup bean, Key<K> key, Supplier<? extends K> supplier) {
        // BindableVariable bv = CODEGEN.get(bean).get(key);

        BindableVariable var = codegenVariables.get(new CodeGeneratorKey(bean, key));

        if (var == null) {
            throw new IllegalArgumentException("The specified bean must have an injection site that uses @" + CodeGenerated.class.getSimpleName() + " " + key);
        } else if (var.isBound()) {
            throw new IllegalStateException("A supplier has previously been provided for key [key = " + key + ", bean = " + bean + "]");
        }

        var.bindGeneratedConstant(supplier);
    }

//    public void provideAll(ServiceLocator locator, Consumer<ServiceTransformer> transformer) {
//        // ST.contract throws UOE
//    }

    final void embed(Assembly assembly) {
        /// MHT til hooks. Saa tror jeg faktisk at man tager de bean hooks
        // der er paa den assembly der definere dem

        // Men der er helt klart noget arbejde der
        throw new UnsupportedOperationException();
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
            public void hookOnAnnotatedField(Annotation hook, OperationalField field) {
                if (hook instanceof Inject) {
                    // Det er jo inject service!???
                    field.newInjectOperation().unwrap();
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
                    extension.container.sm.provide(key, operation, new FromOperation(operation));
                } else {
                    super.hookOnAnnotatedField(hook, field);
                }
            }

            /** Handles {@link Inject}, {@link OnInitialize}, {@link OnStart} and {@link OnStop}. */
            @Override
            public void hookOnAnnotatedMethod(Annotation annotation, OperationalMethod method) {
                BeanSetup bean = BeanSetup.crack(method);
                OperationTemplate temp = OperationTemplate.defaults().withIgnoreReturn();

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
                } else if ((annotation instanceof Provide) || (annotation instanceof Export)) {
                    Key<?> key = method.toKey();
                    boolean isProviding = method.annotations().containsType(Provide.class);
                    boolean isExporting = method.annotations().containsType(Export.class);

                    OperationTemplate temp2 = OperationTemplate.defaults().withReturnType(method.operationType().returnRawType());

                    if (!Modifier.isStatic(method.modifiers())) {
                        if (beanKind() != BeanKind.CONTAINER) {
                            throw new BuildException("Not okay)");
                        }
                    }

                    if (isProviding) {
                        OperationSetup operation = OperationSetup.crack(method.newOperation(temp2));
                        extension.container.sm.provide(key, operation, new FromOperation(operation));
                    }

                    if (isExporting) {
                        OperationSetup operation = OperationSetup.crack(method.newOperation(temp2));
                        extension.container.sm.export(key, operation);
                    }
                } else {
                    super.hookOnAnnotatedMethod(annotation, method);
                }
            }

            /** Handles {@link ContainerGuest}, {@link InvocationArgument} and {@link CodeGenerated}. */
            @Override
            public void hookOnProvidedAnnotatedVariable(Annotation hook, BindableVariable v) {
                if (hook instanceof ContainerGuest) {
                    Variable va = v.variable();
                    if (va.getRawType().equals(String.class)) {
                        v.bindOp(new Op1<@InvocationArgument ApplicationInitializationContext, String>(a -> a.name()) {});
                    } else if (va.getRawType().equals(ManagedLifetimeController.class)) {
                        v.bindOp(new Op1<@InvocationArgument ApplicationInitializationContext, ManagedLifetimeController>(a -> a.cr.runtime) {});
                    } else if (va.getRawType().equals(ServiceLocator.class)) {
                        v.bindOp(new Op1<@InvocationArgument ApplicationInitializationContext, ServiceLocator>(a -> a.serviceLocator()) {});
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

                    v.bindInvocationArgument(index);
                } else if (hook instanceof CodeGenerated cg) {
                    BeanSetup bean = ((PackedBindableVariable) v).operation.bean;
                    if (beanOwner().isApplication()) {
                        throw new BeanInstallationException("@" + CodeGenerated.class.getSimpleName() + " can only be used by extensions");
                    }
                    // Create the key
                    Key<?> key = v.toKey();

                    // CODEGEN.get(this).putIfAbsent(key, v);
                    BindableVariable bv = codegenVariables.putIfAbsent(new CodeGeneratorKey(bean, key), v);
                    if (bv != null) {
                        failWith(key + " Can only be injected once for bean ");
                    }

                } else {
                    super.hookOnProvidedAnnotatedVariable(hook, v);
                }
            }

            @Override
            public void hookOnProvidedVariableType(Class<?> hook, BindableWrappedVariable v) {
                if (hook == ExtensionContext.class) {
                    if (v.availableInvocationArguments().isEmpty() || v.availableInvocationArguments().get(0) != ExtensionContext.class) {
                        // throw new Error(v.availableInvocationArguments().toString());
                    }
                    v.bindInvocationArgument(0);
                } else {
                    v.checkAssignableTo(ExtensionContext.class);
                }
            }

        };
    }

    ContainerInstaller newContainerInstaller() {
        return new PackedContainerInstaller(extension.container);
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

        for (Entry<CodeGeneratorKey, BindableVariable> e : codegenVariables.entrySet()) {
            if (!e.getValue().isBound()) {
                throw new InternalExtensionException(e.getKey().key() + " not bound for bean " + e.getKey().bean());
            }
        }
        // A lifetime root lets order some dependencies
        if (isLifetimeRoot()) {
            extension.container.lifetime.orderDependencies();
        }
    }

    protected void onNew(BaseExtensionPoint p) {
        // BeanConfiguration b = install(PackedServiceLocator.class);
        // p.addCodeGenerated(b, new Key<Map<Key<?>, MethodHandle>>() {}, null);
    }

    /**
     * Provides every service from the specified locator.
     *
     * @param locator
     *            the locator to provide services from
     */
    public void provideAll(ServiceLocator locator) {
        requireNonNull(locator, "locator is null");
        checkIsConfigurable();
        throw new UnsupportedOperationException();
    }

    // Think we need installPrototype (Which will fail if not provided or exported)
    // providePrototype would then be installPrototype().provide() // not ideal
    // Men taenker vi internt typisk arbejde op i mod implementering. Dog ikke altid
    // providePerRequest <-- every time the service is requested
    // Also these beans, can typically just be composites??? Nah
    public <T> ProvideableBeanConfiguration<T> providePrototype(Class<T> implementation) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.MANYTON).lifetimes(OperationTemplate.defaults()).install(implementation);
        return new ProvideableBeanConfiguration<>(handle).provide();
    }

    public <T> ProvideableBeanConfiguration<T> providePrototype(Op<T> op) {
        BeanHandle<T> handle = newBeanInstaller(BeanKind.MANYTON).lifetimes(OperationTemplate.defaults()).install(op);
        return new ProvideableBeanConfiguration<>(handle).provide();
    }

    // requires bliver automatisk anchoret...
    // anchorAllChildExports-> requireAllChildExports();
    public void serviceRequire(Class<?>... keys) {
        serviceRequire(Key.ofAll(keys));
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
    public void serviceRequire(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkIsConfigurable();
        // ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE);
        for (@SuppressWarnings("unused")
        Key<?> key : keys) {
            // delegate.ios.requirementsOrCreate().require(key, false /* , cs */);
            throw new UnsupportedOperationException();
        }
    }

    public void serviceRequireOptionally(Class<?>... keys) {
        serviceRequireOptionally(Key.ofAll(keys));
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
    public void serviceRequireOptionally(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkIsConfigurable();
        // ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE_OPTIONAL);
        for (@SuppressWarnings("unused")
        Key<?> key : keys) {
            throw new UnsupportedOperationException();
//            delegate.ios.requirementsOrCreate().require(key, true /* , cs */);
        }
    }

    private record CodeGeneratorKey(BeanSetup bean, Key<?> key) {}
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

class ZServiceSandbox {

    // Validates the outward facing contract
    public void checkContract(Consumer<? super ServiceContract> validator) {}

    // Det der er ved validator ServiceContractChecker er at man kan faa lidt mere context med
    // checkContract(Service(c->c.checkExact(sc));// ContractChecker.exact(sc));
    public void checkContractExact(ServiceContract sc) {}

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
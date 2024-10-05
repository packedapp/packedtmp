package app.packed.extension;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.application.ApplicationMirror;
import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanClassMutator;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanElement.BeanField;
import app.packed.bean.BeanElement.BeanMethod;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanTemplate;
import app.packed.bean.Inject;
import app.packed.bean.ManagedBeanRequiredException;
import app.packed.bean.SyntheticBean;
import app.packed.binding.BindableVariable;
import app.packed.binding.Key;
import app.packed.binding.UnwrappedBindableVariable;
import app.packed.build.BuildException;
import app.packed.build.action.BuildActionable;
import app.packed.component.guest.ComponentHostContext;
import app.packed.component.guest.FromComponentGuest;
import app.packed.container.ContainerBuildLocal;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerMirror;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import app.packed.lifecycle.OnInitialize;
import app.packed.lifecycle.OnStart;
import app.packed.lifecycle.OnStartContext;
import app.packed.lifecycle.OnStop;
import app.packed.lifecycle.OnStopContext;
import app.packed.lifetime.Main;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTemplate;
import app.packed.service.Export;
import app.packed.service.ProvidableBeanConfiguration;
import app.packed.service.Provide;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceNamespaceConfiguration;
import app.packed.service.sandbox.ServiceOutgoingTransformer;
import internal.app.packed.application.GuestBeanHandle;
import internal.app.packed.application.deployment.DeploymentMirror;
import internal.app.packed.bean.BeanLifecycleOrder;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.bean.PackedBeanInstaller.ProvidableBeanHandle;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.binding.BindingAccessor.FromOperationResult;
import internal.app.packed.binding.PackedBindableWrappedVariable;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.context.PackedComponentHostContext;
import internal.app.packed.entrypoint.OldEntryPointSetup;
import internal.app.packed.entrypoint.OldEntryPointSetup.MainThreadOfControl;
import internal.app.packed.lifetime.packed.OnInitializeOperationHandle;
import internal.app.packed.lifetime.packed.OnStartOperationHandle;
import internal.app.packed.lifetime.packed.OnStopOperationHandle;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.PackedServiceLocator;
import internal.app.packed.util.handlers.BeanHandlers;

/**
 * An extension that defines the foundational APIs for managing beans, services, containers and applications.
 * <p>
 * Every container automatically uses this extension. And every extension automatically has a direct dependency on this
 * extension.
 * <p>
 * All methods on this class deals with beans Table area [bean,container,service] prefix desciption
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

public final class BaseExtension extends FrameworkExtension<BaseExtension> {

    // We use an initial value for now, because we share FromLinks and the boolean fields
    // But right now we only have a single field
    static final ContainerBuildLocal<FromLinks> FROM_LINKS = ContainerBuildLocal.of(FromLinks::new);

    /**
     * All your base are belong to us.
     *
     * @param handle
     *            the extension's handle
     */
    BaseExtension(ExtensionHandle<BaseExtension> handle) {
        super(handle);
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

        extension.container.servicesMain().exportAll = true;
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
    @BuildActionable("bean.install")
    public <T> ProvidableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = install0(BeanKind.CONTAINER.template()).install(implementation, ProvidableBeanHandle::new);
        return h.configuration();
    }

    /**
     * Installs a component that will use the specified {@link Op} to instantiate the component instance.
     *
     * @param op
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Op)
     */
    public <T> ProvidableBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = install0(BeanKind.CONTAINER.template()).install(op, ProvidableBeanHandle::new);
        return h.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> install(SyntheticBean<T> synthetic) {
        throw new UnsupportedOperationException();
    }

    private PackedBeanInstaller install0(BeanTemplate template) {
        return ((PackedBeanTemplate) template).newInstaller(extension, extension.container.assembly);
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
    public <T> ProvidableBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = install0(BeanKind.CONTAINER.template()).installInstance(instance, ProvidableBeanHandle::new);
        return h.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> installLazy(Class<T> implementation) {
        BeanHandle<ProvidableBeanConfiguration<T>> handle = install0(BeanKind.LAZY.template()).install(implementation, ProvidableBeanHandle::new);
        return handle.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> installLazy(Op<T> op) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = install0(BeanKind.LAZY.template()).install(op, ProvidableBeanHandle::new);
        return h.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> installLazy(SyntheticBean<T> synthetic) {
        throw new UnsupportedOperationException();
    }

    // Kan ikke se den her giver mening andet end som provide();
    public <T> ProvidableBeanConfiguration<T> installPrototype(Class<T> implementation) {
        BeanHandle<ProvidableBeanConfiguration<T>> handle = install0(BeanKind.UNMANAGED.template()).install(implementation, ProvidableBeanHandle::new);
        return handle.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> installPrototype(Op<T> op) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = install0(BeanKind.UNMANAGED.template()).install(op, ProvidableBeanHandle::new);
        return h.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> installPrototype(SyntheticBean<T> synthetic) {
        // fail for instance
        throw new UnsupportedOperationException();
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
        BeanHandle<BeanConfiguration> handle = install0(BeanKind.STATIC.template()).install(implementation);
        return handle.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> installStatic(SyntheticBean<T> synthetic) {
        throw new UnsupportedOperationException();
    }

    /**
     * Installs an exported service locator.
     *
     * @see BaseExtensionPoint#EXPORTED_SERVICE_LOCATOR
     */
    void lifetimeExportServiceLocator() {
        // Create a new bean that holds the ServiceLocator to export
        // will fail if installed multiple times

        BeanHandle<ProvidableBeanConfiguration<PackedServiceLocator>> ha = newBeanBuilderSelf(BeanKind.CONTAINER.template()).install(PackedServiceLocator.class,
                ProvidableBeanHandle::new);
        ha.configuration().exportAs(ServiceLocator.class);

        // PackedServiceLocator needs a Map<Key, MethodHandle> which is created in the code generation phase
        BeanSetup.crack(ha).bindCodeGenerator(new Key<Map<Key<?>, MethodHandle>>() {}, () -> extension.container.servicesMain().exportedServices());

        // Alternative, If we do not use it for anything else
        newBeanBuilderSelf(BeanKind.CONTAINER.template()).installIfAbsent(PackedServiceLocator.class, BeanConfiguration.class, BeanHandle::new, bh -> {
            bh.exportAs(ServiceLocator.class);
            BeanSetup.crack(bh).bindCodeGenerator(new Key<Map<Key<?>, MethodHandle>>() {}, () -> extension.container.servicesMain().exportedServices());
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
    // Why not on ContainerConfiguration. Think because I wanted to keep it clean
    public void link(String name, Assembly assembly, Wirelet... wirelets) {
        link0().named(name).install(assembly, wirelets);
    }

    /**
     * Creates a new container that strongly linked to the lifetime of this container.
     *
     * @param wirelets
     *            optional wirelets
     * @return configuration for the new container
     */
    // addContainer??? Yeah it is not linked
    public ContainerConfiguration link(Wirelet... wirelets) {
        ContainerHandle<?> handle = link0().install(wirelets);
        return handle.configuration();
    }

    /** {@return a new container builder used for linking.} */
    private ContainerTemplate.Installer<?> link0() {
        return PackedContainerInstaller.of((PackedContainerTemplate<?>) ContainerTemplate.DEFAULT, BaseExtension.class, extension.container.application,
                extension.container);
    }

    void linkPostfix(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    void linkPrefix(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a special bean builder that can install beans for BaseExtension. Where BaseExtension is both the owner and
     * installer.
     *
     * @param template
     *            a template for the bean
     * @return a bean installer
     */
    private BeanInstaller newBeanBuilderSelf(BeanTemplate template) {
        return ((PackedBeanTemplate) template).newInstaller(extension, extension);
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
            private static final OperationTemplate BEAN_LIFECYCLE_TEMPLATE = OperationTemplate.defaults().reconfigure(c -> c.returnIgnore());

            static final ContextTemplate ON_START_CONTEXT_TEMPLATE = ContextTemplate.of(OnStartContext.class, c -> {});

            static final ContextTemplate ON_STOP_CONTEXT_TEMPLATE = ContextTemplate.of(OnStopContext.class, c -> {});

            private static final OperationTemplate BEAN_LIFECYCLE_ON_START_TEMPLATE = OperationTemplate.defaults()
                    .reconfigure(c -> c.returnIgnore().inContext(ON_START_CONTEXT_TEMPLATE));

            private static final OperationTemplate BEAN_LIFECYCLE_ON_STOP_TEMPLATE = OperationTemplate.defaults()
                    .reconfigure(c -> c.returnIgnore().inContext(ON_STOP_CONTEXT_TEMPLATE));

            /** Handles {@link Inject}. */
            @Override
            public void onAnnotatedField(Annotation hook, BeanField field) {
                if (hook instanceof Inject) {
                    // checkNotStatic
                    // Det er jo inject service!???
                    // field.newBindableVariable().unwrap();
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

                    OperationSetup operation = OperationSetup.crack(field.newGetOperation(OperationTemplate.defaults()).install(OperationHandle::new));

                    BeanSetup bean = BeanHandlers.invokeBeanIntrospectorBean(this);
                    // Hmm, vi har jo slet ikke lavet namespacet endnu
                    bean.serviceNamespace().provideOperation(key, operation, new FromOperationResult(operation));
                } else {
                    super.onAnnotatedField(hook, field);
                }
            }

            /** Handles {@link ContainerGuest}. */
            @Override
            public void onAnnotatedVariable(Annotation annotation, BindableVariable v) {
                if (annotation instanceof FromComponentGuest) {
                    beanHandle(GuestBeanHandle.class).get().resolve(this, v);
                } else {
                    super.onAnnotatedVariable(annotation, v);
                }
            }

            @Override
            public void onContextualServiceProvision(Key<?> key, Class<?> actualHook, Set<Class<? extends Context<?>>> contexts,
                    UnwrappedBindableVariable binding) {
                Class<?> hook = key.rawType();
                OperationSetup operation = ((PackedBindableWrappedVariable) binding).var().operation;

                if (ApplicationLaunchContext.class.isAssignableFrom(hook)) {
                    binding.bindContext(ApplicationLaunchContext.class);
                } else if (hook == ExtensionContext.class) {
                    // We probably should have failed already, so no need to check. Only beans that are in the context
                    if (beanAuthor().isApplication()) {
                        binding.failWith(hook.getSimpleName() + " can only be injected into bean that owned by an extension");
                    }
                    binding.bindContext(ExtensionContext.class);
                } else if (hook == ComponentHostContext.class) {
                    PackedComponentHostContext c = beanHandle(GuestBeanHandle.class).get().toContext();
                    binding.bindInstance(c);
                } else if (hook == OnStartContext.class) {
                    // v.bindInvocationArgument(1);
                    binding.bindContext(OnStartContext.class);
                } else if (hook == OnStopContext.class) {
                    // v.bindInvocationArgument(1);
                    binding.bindContext(OnStopContext.class);
                }

                // MIRRORS
                else if (actualHook == DeploymentMirror.class) {
                    binding.bindInstance(operation.bean.container.application.deployment.mirror());
                } else if (actualHook == ApplicationMirror.class) {
                    binding.bindInstance(operation.bean.container.application.mirror());
                } else if (actualHook == ContainerMirror.class) {
                    binding.bindInstance(operation.bean.container.mirror());
                } else if (actualHook == AssemblyMirror.class) {
                    binding.bindInstance(operation.bean.container.assembly.mirror());
                } else if (actualHook == BeanMirror.class) {
                    binding.bindInstance(operation.bean.mirror());
                } else if (actualHook == OperationMirror.class) {
                    binding.bindInstance(operation.mirror());
                } else {
                    // will always fail
                    binding.checkAssignableTo(ExtensionContext.class, DeploymentMirror.class, ApplicationMirror.class, ContainerMirror.class,
                            AssemblyMirror.class, BeanMirror.class, OperationMirror.class);
                }
            }

            private <H extends OperationHandle<?>> void checkNotStaticBean(Class<? extends Annotation> annotationType) {
                if (beanKind() == BeanKind.STATIC) {
                    throw new ManagedBeanRequiredException(annotationType + " is not supported for static beans");
                }
            }

            /** Handles {@link Inject}, {@link OnInitialize}, {@link OnStart} and {@link OnStop}. */
            @Override
            public void onAnnotatedMethod(Annotation annotation, BeanMethod method) {
                BeanSetup bean = BeanHandlers.invokeBeanIntrospectorBean(this);

                if (annotation instanceof Inject) {
                    checkNotStaticBean(Inject.class);
                    OperationHandle<?> handle = method.newOperation(BEAN_LIFECYCLE_TEMPLATE).install(OperationHandle::new);
                    bean.operations.addLifecycleOperation(BeanLifecycleOrder.INJECT, handle);
                } else if (annotation instanceof OnInitialize oi) {
                    checkNotStaticBean(OnInitialize.class);
                    OperationHandle<?> handle = method.newOperation(BEAN_LIFECYCLE_TEMPLATE).install(i -> new OnInitializeOperationHandle(i, oi));
                    bean.operations.addLifecycleOperation(BeanLifecycleOrder.fromInitialize(oi.order()), handle);
                } else if (annotation instanceof OnStart oi) {
                    checkNotStaticBean(OnStart.class);
                    OperationHandle<?> handle = method.newOperation(BEAN_LIFECYCLE_ON_START_TEMPLATE).install(i -> new OnStartOperationHandle(i, oi));
                    bean.operations.addLifecycleOperation(BeanLifecycleOrder.fromStarting(oi.order()), handle);
                } else if (annotation instanceof OnStop oi) {
                    checkNotStaticBean(OnStop.class);
                    OperationHandle<?> handle = method.newOperation(BEAN_LIFECYCLE_ON_STOP_TEMPLATE).install(i -> new OnStopOperationHandle(i, oi));
                    bean.operations.addLifecycleOperation(BeanLifecycleOrder.fromStopping(oi.order()), handle);
                } else if (annotation instanceof Provide) {
                    OperationTemplate temp2 = OperationTemplate.defaults().reconfigure(c -> c.returnType(method.operationType().returnRawType()));
                    if (!Modifier.isStatic(method.modifiers())) {
                        if (beanKind() != BeanKind.CONTAINER) {
                            throw new BeanInstallationException("Not okay)");
                        }
                    }
                    OperationSetup operation = OperationSetup.crack(method.newOperation(temp2).install(OperationHandle::new));
                    bean.container.servicesMain().provideOperation(method.toKey(), operation, new FromOperationResult(operation));
                } else if (annotation instanceof Export) {
                    OperationTemplate temp2 = OperationTemplate.defaults().reconfigure(c -> c.returnType(method.operationType().returnRawType()));

                    if (!Modifier.isStatic(method.modifiers())) {
                        if (beanKind() != BeanKind.CONTAINER) {
                            throw new BeanInstallationException("Not okay)");
                        }
                    }
                    OperationSetup operation = OperationSetup.crack(method.newOperation(temp2).install(OperationHandle::new));
                    bean.container.servicesMain().export(method.toKey(), operation);
                } else if (annotation instanceof Main) {
                    if (!isInApplicationLifetime()) {
                        throw new BeanInstallationException("Must be in the application lifetime to use @" + Main.class.getSimpleName());
                    }

                    bean.container.lifetime.entryPoints.takeOver(BaseExtension.this, BaseExtension.class);

                    bean.container.lifetime.entryPoints.entryPoint = new OldEntryPointSetup();

                    OperationTemplate temp = OperationTemplate.defaults().reconfigure(c -> c.returnType(method.operationType().returnRawType()));
                    OperationHandle<?> os = method.newOperation(temp).install(OperationHandle::new);

                    MainThreadOfControl mc = bean.container.lifetime.entryPoints.entryPoint.mainThread();

                    os.generateMethodHandleOnCodegen(mh -> mc.generatedMethodHandle = mh);
                } else {
                    super.onAnnotatedMethod(annotation, method);
                }
            }
        };
    }

    /** {@return a mirror for this extension.} */
    @Override
    protected BaseExtensionMirror newExtensionMirror() {
        return new BaseExtensionMirror(handle());
    }

    /** {@inheritDoc} */
    @Override
    protected BaseExtensionPoint newExtensionPoint(ExtensionUseSite usesite) {
        return new BaseExtensionPoint(usesite);
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

        // A lifetime root lets order some dependencies
        if (isLifetimeRoot()) {
            extension.container.lifetime.orderDependencies();
        }
    }

    /** {@return the container's main service namespace} */
    public ServiceNamespaceConfiguration services() {
        return services("main");
    }

    public ServiceNamespaceConfiguration services(String name) {
        // Will automatically create one with default settings
        throw new UnsupportedOperationException();
    }

    protected ServiceNamespaceConfiguration services(String name, Consumer<?> newConfiguration) {
        return services("main");
    }

    // transformAllBeans() <-- includes extension beans... (Must be open)
    public Runnable transformAllBeans(Consumer<? super BeanClassMutator> transformer) {
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
    public Runnable transformBeans(Consumer<? super BeanClassMutator> transformer) {
        throw new UnsupportedOperationException();
    }

    // Har man brug for andet end class transformer? Er der nogle generalle bean properties??? IDK
    <T> ProvidableBeanConfiguration<T> transformingInstall(Class<T> implementation, Consumer<? super BeanClassMutator> transformation) {
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
    // Det maa sgu blive en extra metode
    // BeanInstaller.forceOpBind("XOp", 2, "FooBar");

    public void transformNextBean(Consumer<? super BeanClassMutator> transformer) {}

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
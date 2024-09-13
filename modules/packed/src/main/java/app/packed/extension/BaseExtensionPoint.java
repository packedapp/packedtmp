package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.operation.Op;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationDependencyOrder;
import app.packed.operation.OperationTemplate;
import app.packed.runtime.RunState;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceableBeanConfiguration;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.container.PackedExtensionPointContext;
import sandbox.extension.container.ComponentGuestAdaptorBeanConfiguration;
import sandbox.extension.container.ContainerTemplate;
import sandbox.extension.container.ContainerTemplateLink;
import sandbox.lifetime.external.LifecycleController;

/** An {@link ExtensionPoint extension point} for {@link BaseExtension}. */
public class BaseExtensionPoint extends ExtensionPoint<BaseExtension> {

    // Altsaa fx naar vi naar hen til application.
    // Vi kan jo ikke installere den i extensionen...
    public static ContainerTemplateLink CONTAINER_MIRROR = ContainerTemplateLink.of(MethodHandles.lookup(), BaseExtension.class, "ContainerMirror").build();

    /** A bridge that makes the name of the container available. */
    public static final ContainerTemplateLink CONTAINER_NAME = null;

    /**
     * A container lifetime channel that makes the container's exported services available as
     * {@link app.packed.service.ServiceLocator}.
     */
    public static final ContainerTemplateLink EXPORTED_SERVICE_LOCATOR = baseBuilder("ExportedServiceLocator")
            .localConsume(BaseExtension.FROM_LINKS, t -> t.exportServices = true).provideExpose(ServiceLocator.class).build();

    // Teanker vi altid exportere den
    // check that we have a managed lifetime. Maybe PackedManagedBeanController is already installed
    // baseExtension.managedLifetimeBean.export(); // maybe it is already exported

    public static final ContainerTemplateLink MANAGED_LIFETIME_CONTROLLER = baseBuilder("ManagedLifetimeController").provideExpose(LifecycleController.class)
            .build();

    /** Creates a new base extension point. */
    BaseExtensionPoint() {}

    public <T> ServiceableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<ServiceableBeanConfiguration<T>> h = newBean(BeanKind.CONTAINER.template(), context()).install(implementation,
                ServiceableBeanConfiguration::new);
        return h.configuration();
    }

    /**
     * @param <T>
     *            the type of bean to install
     * @param op
     *            an operation responsible for creating an instance of the bean when the container is initialized
     * @return a configuration object representing the installed bean
     */
    public <T> ServiceableBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<ServiceableBeanConfiguration<T>> h = newBean(BeanKind.CONTAINER.template(), context()).install(op, ServiceableBeanConfiguration::new);
        return h.configuration();
    }

    // Can I come up with a situation where we want multiple guests of the same type??
    // I think not
    public <T> ComponentGuestAdaptorBeanConfiguration<T> installContainerHost(Class<T> holderClass) {
        throw new UnsupportedOperationException();
    }

    public <T> ComponentGuestAdaptorBeanConfiguration<T> installContainerHost(Op<T> holderClass) {
        throw new UnsupportedOperationException();
    }
//
//    public <T> ContainerHolderConfiguration<T> containerHolderInstallIfAbsent(Class<T> holderClass, Consumer<? super ContainerHolderConfiguration<T>> action) {
//        throw new UnsupportedOperationException();
//    }

//    // Contexts
//    public ContainerBuilder containerInstallerExistingLifetime(boolean isLazy) {
//        // Kan only use channels that are direct dependencies of the usage extension
//
//        ExtensionSetup s = contextUse().usedBy();
//        return new PackedContainerBuilder(ContainerTemplate.IN_PARENT, s.extensionType, s.container.application, s.container);
//    }

    public <T> InstanceBeanConfiguration<T> installIfAbsent(Class<T> clazz) {
        return installIfAbsent(clazz, c -> {});
    }

    /**
     * <p>
     * The configuration might be di
     *
     * @param <T>
     *            the type of bean to install
     * @param clazz
     * @param action
     * @return a bean configuration
     * @implNote the implementation may use to return different bean configuration instances for subsequent invocations.
     *           Even for action and the returned bean
     */
    @SuppressWarnings("unchecked")
    public <T> InstanceBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super InstanceBeanConfiguration<T>> action) {
        requireNonNull(action, "action is null");
        BeanHandle<?> handle = newBean(BeanKind.CONTAINER.template(), context()).installIfAbsent(clazz, InstanceBeanConfiguration.class,
                InstanceBeanConfiguration::new, h -> action.accept((InstanceBeanConfiguration<T>) h.configuration()));
        return (InstanceBeanConfiguration<T>) handle.configuration();
    }

    public <T> InstanceBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<InstanceBeanConfiguration<T>> h = newBean(BeanKind.CONTAINER.template(), context()).installInstance(instance,
                InstanceBeanConfiguration::new);
        return h.configuration();
    }

//    // Vi bliver jo noedt til at have en baade med og uden use site
//    public FunctionalBeanConfiguration installFunctional() {
//        PackedBeanHandleBuilder bb = (PackedBeanHandleBuilder) beanBuilderForExtension(BeanKind.STATIC.template(), context());
//        BeanHandle<?> handle = bb.installSourceless();
//        return new FunctionalBeanConfiguration(handle);
//    }

    /**
     * Installs a {@link BeanKind#STATIC static} bean.
     *
     * @param beanClass
     *            the type of static bean to install
     * @return a configuration object representing the installed bean
     */
    public BeanConfiguration installStatic(Class<?> beanClass) {
        return newBean(BeanKind.STATIC.template(), context()).install(beanClass, BeanConfiguration::new).configuration();
    }

    /**
     * Creates a new bean installer to be able to install a new bean on behalf of the user.
     *
     * @param template
     *            a bean template representing the behaviour of the new bean
     * @return the installer
     */
    public BeanTemplate.Installer newBean(BeanTemplate template) {
        return ((PackedBeanTemplate) template).newInstaller(extension().extension, extension().extension.container.assembly);
    }

    /**
     * Creates a new bean installer to be able to install a new bean on behalf of a another extension.
     *
     * @param template
     *            a bean template representing the behaviour of the new bean
     * @return the installer
     */
    public BeanTemplate.Installer newBean(BeanTemplate template, UseSite forExtension) {
        // Skal den her overhovede vaere public???
        // Maybe move this to UseSite.. Nah other extension should also allow to install components.
        // Where we cannot put the methods on usesite
        requireNonNull(forExtension, "forExtension is null");
        return ((PackedBeanTemplate) template).newInstaller(extension().extension, ((PackedExtensionPointContext) forExtension).usedBy());
    }

    /**
     * Creates a new container installer to be able to install a new container on behalf of the user.
     *
     * @param template
     *            a container template representing the behaviour of the new container
     * @return the installer
     */

    public ContainerTemplate.Installer newContainer(ContainerTemplate template) {
        // Kan only use channels that are direct dependencies of the usage extension
        ExtensionSetup es = contextUse().usedBy();
        return PackedContainerInstaller.of((PackedContainerTemplate) template, es.extensionType, es.container.application, es.container);
    }

    public int registerEntryPoint(Class<?> hook) {
        return super.extensionSetup().container.lifetime.entryPoints.takeOver(extension(), usedBy());// .registerEntryPoint(usedBy(), isMain);
    }

    public OperationConfiguration runLifecycleOperation(OperationTemplate.Installer operation, RunState state, OperationDependencyOrder ordering) {
        throw new UnsupportedOperationException();
    }

    public OperationConfiguration runOnBeanInitialization(OperationTemplate.Installer operation, OperationDependencyOrder ordering) {
        requireNonNull(ordering, "ordering is null");
        throw new UnsupportedOperationException();
//        OperationHandle handle = h.newOperation(OperationTemplate.defaults(), context());
//        ((PackedOperationHandle) handle).operation().bean.addLifecycleOperation(BeanLifecycleOrder.fromInitialize(ordering), handle);
//        return new OperationConfiguration(handle);
    }

    /**
     * Creates a new inject operation from the specified delegating operation handle.
     *
     * @param h
     *            the delegating handle that the operation should be created from.
     * @return a configuration object representing the inject operation
     * @see Inject
     */
    public OperationConfiguration runOnBeanInject(OperationTemplate.Installer operation) {
//        PackedOperationHandle handle = (PackedOperationHandle) h.newOperation(OperationTemplate.defaults(), context());
//        handle.operation().bean.addLifecycleOperation(BeanLifecycleOrder.INJECT, handle);
//        return new OperationConfiguration(handle);
        throw new UnsupportedOperationException();
    }

//
//    public OperationConfiguration runOnBeanStart(DelegatingOperationHandle h, LifecycleOrder ordering) {
//        // What if I want to fork it??? on OC??
//        // Or do I need to call it immediately
//        // runOnLifecycle(RunState runstate, LifecycleOrder ordering)
//        throw new UnsupportedOperationException();
//    }
//
//    public OperationConfiguration runOnBeanStop(DelegatingOperationHandle h, LifecycleOrder ordering) {
//        throw new UnsupportedOperationException();
//    }

    private static ContainerTemplateLink.Configurator baseBuilder(String name) {
        return ContainerTemplateLink.of(MethodHandles.lookup(), BaseExtension.class, name);
    }
}

//// onExtension E newContainer(Wirelet wirelets); // adds this to the container and returns it
//
//public ContainerHandle newContainer(Assembly assembly, Wirelet... wirelets) {
//  throw new UnsupportedOperationException();
//}
//
//public ContainerHandle newContainer(Wirelet... wirelets) {
//  // What is the usecase here without
//  // Okay I want to a create a container in the container.
//  // And then add myself
//
//  // Let's say entity beans are always in their own container
//  // newContainer().useMyself().
//
//  throw new UnsupportedOperationException();
//}
//
//public <T> ContainerGuestBeanConfiguration<T> newContainerGuest(Class<T> containerGuest, ExtensionLifetimeBridge... bridges) {
//  throw new UnsupportedOperationException();
//}
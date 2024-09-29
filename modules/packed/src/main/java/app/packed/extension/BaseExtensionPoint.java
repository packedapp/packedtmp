package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.component.guest.ContainerTemplateLink;
import app.packed.container.ContainerTemplate;
import app.packed.context.ContextTemplate;
import app.packed.operation.Op;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationDependencyOrder;
import app.packed.operation.OperationTemplate;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;
import app.packed.service.ProvidableBeanConfiguration;
import app.packed.service.ServiceLocator;
import internal.app.packed.bean.PackedBeanInstaller.ProvidableBeanHandle;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionUseSite;
import internal.app.packed.lifetime.runtime.PackedExtensionContext;

/** An {@link ExtensionPoint extension point} for {@link BaseExtension}. */
public final class BaseExtensionPoint extends ExtensionPoint<BaseExtension> {

    final static BeanTemplate CONTAINER;

    public static ContainerTemplateLink CONTAINER_MIRROR = ContainerTemplateLink.of(MethodHandles.lookup(), BaseExtension.class, "ContainerMirror").build();

    /**
     * A container lifetime channel that makes the container's exported services available as
     * {@link app.packed.service.ServiceLocator}.
     */
    public static final ContainerTemplateLink EXPORTED_SERVICE_LOCATOR = baseBuilder("ExportedServiceLocator")
            .localConsume(BaseExtension.FROM_LINKS, t -> t.exportServices = true).provideExpose(ServiceLocator.class).build();

    public static final ContainerTemplateLink MANAGED_LIFETIME = baseBuilder(ManagedLifecycle.class.getSimpleName()).provideExpose(ManagedLifecycle.class)
            .build();

    static {
        ContextTemplate ct = ContextTemplate.of(ExtensionContext.class, c -> c.implementationClass(PackedExtensionContext.class));
        CONTAINER = BeanKind.CONTAINER.template().reconfigure(c -> {
            c.inContext(ct);
        });
    }

    /** Creates a new base extension point. */
    BaseExtensionPoint(ExtensionUseSite usesite) {
        super(usesite);
    }

    public <T> ProvidableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = newBean(CONTAINER, context()).install(implementation, ProvidableBeanHandle::new);
        return h.configuration();
    }

    /**
     * @param <T>
     *            the type of bean to install
     * @param op
     *            an operation responsible for creating an instance of the bean when the container is initialized
     * @return a configuration object representing the installed bean
     */
    public <T> ProvidableBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = newBean(CONTAINER, context()).install(op, ProvidableBeanHandle::new);
        return h.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> installIfAbsent(Class<T> clazz) {
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> ProvidableBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super ProvidableBeanConfiguration<T>> action) {
        requireNonNull(action, "action is null");
        Function<BeanTemplate.Installer, ProvidableBeanHandle<?>> f = ProvidableBeanHandle::new;
        BeanHandle<?> handle = newBean(CONTAINER, context()).installIfAbsent(clazz, ProvidableBeanConfiguration.class, (Function) f,
                h -> action.accept((ProvidableBeanConfiguration<T>) h.configuration()));
        return (ProvidableBeanConfiguration<T>) handle.configuration();
    }

    /**
     * Installs a new bean instance.
     *
     * @param <T>
     *            the type of instance
     * @param instance
     *            the bean instance
     * @return a providable configuration for the new bean
     */
    public <T> ProvidableBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = newBean(CONTAINER, context()).installInstance(instance, ProvidableBeanHandle::new);
        return h.configuration();
    }

    /**
     * Installs a {@link BeanKind#STATIC static} bean.
     *
     * @param beanClass
     *            the type of static bean to install
     * @return a configuration object representing the installed bean
     */
    public BeanConfiguration installStatic(Class<?> beanClass) {
        return newBean(BeanKind.STATIC.template(), context()).install(beanClass, BeanHandle::new).configuration();
    }

    /**
     * Creates a new bean installer to be able to install a new bean on behalf of the user.
     *
     * @param template
     *            a bean template representing the behaviour of the new bean
     * @return the installer
     */
    public BeanTemplate.Installer newBean(BeanTemplate template) {
        PackedBeanTemplate t = (PackedBeanTemplate) template;
        ExtensionSetup e = usesite.usedBy();
        return t.newInstaller(e, e.container.assembly);
    }

    /**
     * Creates a new bean installer to be able to install a new bean on behalf of a another extension.
     *
     * @param template
     *            a bean template representing the behaviour of the new bean
     * @return the installer
     */
    public BeanTemplate.Installer newBean(BeanTemplate template, ExtensionUseSite forExtension) {
        requireNonNull(forExtension, "forExtension is null");
        PackedBeanTemplate t = (PackedBeanTemplate) template;
        return t.newInstaller(extension().extension, ((PackedExtensionUseSite) forExtension).usedBy());
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
        ExtensionSetup es = usesite.usedBy();
        return PackedContainerInstaller.of((PackedContainerTemplate) template, es.extensionType, es.container.application, es.container);
    }

    public int registerEntryPoint(Class<?> hook) {
        return usesite.extension().container.lifetime.entryPoints.takeOver(extension(), usedBy());// .registerEntryPoint(usedBy(), isMain);
    }

    private static ContainerTemplateLink.Configurator baseBuilder(String name) {
        return ContainerTemplateLink.of(MethodHandles.lookup(), BaseExtension.class, name);
    }
}

//// Vi bliver jo noedt til at have en baade med og uden use site
//public FunctionalBeanConfiguration installFunctional() {
//  PackedBeanHandleBuilder bb = (PackedBeanHandleBuilder) beanBuilderForExtension(BeanKind.STATIC.template(), context());
//  BeanHandle<?> handle = bb.installSourceless();
//  return new FunctionalBeanConfiguration(handle);
//}

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

class unknown {

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

}

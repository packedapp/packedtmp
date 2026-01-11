package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import app.packed.bean.Bean;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanKind;
import app.packed.component.OldContainerTemplateLink;
import app.packed.component.SidehandleBeanConfiguration;
import app.packed.component.SidehandleContext;
import app.packed.component.SidehandleTargetKind;
import app.packed.container.ContainerInstaller;
import app.packed.lifecycle.runtime.ManagedLifecycle;
import app.packed.operation.Op;
import app.packed.service.ProvidableBeanConfiguration;
import app.packed.service.ServiceLocator;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.bean.PackedBeanInstaller.ProvidableBeanHandle;
import internal.app.packed.bean.sidehandle.SidehandleBeanHandle;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionPointHandle;

/** An {@link ExtensionPoint extension point} for {@link BaseExtension}. */
public final class BaseExtensionPoint extends ExtensionPoint<BaseExtension> {

    public static OldContainerTemplateLink CONTAINER_MIRROR = OldContainerTemplateLink.of(MethodHandles.lookup(), BaseExtension.class, "ContainerMirror")
            .build();

    /**
     * A container lifetime channel that makes the container's exported services available as
     * {@link app.packed.service.ServiceLocator}.
     */
    public static final OldContainerTemplateLink EXPORTED_SERVICE_LOCATOR = baseBuilder("ExportedServiceLocator")
            .localConsume(BaseExtension.FROM_LINKS, t -> t.exportServices = true).provideExpose(ServiceLocator.class).build();

    public static final OldContainerTemplateLink MANAGED_LIFETIME = baseBuilder(ManagedLifecycle.class.getSimpleName()).provideExpose(ManagedLifecycle.class)
            .build();

    /** Creates a new base extension point. */
    public BaseExtensionPoint(ExtensionPointHandle usesite) {
        super(usesite);
    }

    public <T> ProvidableBeanConfiguration<T> install(Bean<T> bean) {
        ProvidableBeanHandle<T> h = newBean(BeanKind.SINGLETON, handle()).install(bean, ProvidableBeanHandle::new);
        return h.configuration();
    }

    public <T> ProvidableBeanConfiguration<T> install(Class<T> implementation) {
        return install(Bean.of(implementation));
    }

    /**
     * @param <T>
     *            the type of bean to install
     * @param op
     *            an operation responsible for creating an instance of the bean when the container is initialized
     * @return a configuration object representing the installed bean
     */
    public <T> ProvidableBeanConfiguration<T> install(Op<T> op) {
        return install(Bean.of(op));
    }

    public <T> ProvidableBeanConfiguration<T> installIfAbsent(Class<T> clazz) {
        return installIfAbsent(clazz, _ -> {});
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
    public <T> ProvidableBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super ProvidableBeanConfiguration<T>> action) {
        requireNonNull(action, "action is null");
        return newBean(BeanKind.SINGLETON, handle())
                .installIfAbsent(clazz, ProvidableBeanHandle.class, ProvidableBeanHandle<T>::new, h -> action.accept(h.configuration())).configuration();
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
        return install(Bean.ofInstance(instance));
    }

    // maybe replace SidehandleTargetKind with Class<? extends ComponentHandle>>
    public <T> SidehandleBeanConfiguration<T> installSidebeanIfAbsent(Class<T> implementation, SidehandleTargetKind targetKind, Consumer<? super SidehandleBeanConfiguration<T>> installationAction) {
        BeanInstaller installer = newBean(BeanKind.SIDEHANDLE, handle()).addContext(SidehandleContext.class);
        SidehandleBeanHandle<T> h = installer.installIfAbsent(implementation, SidehandleBeanHandle.class, SidehandleBeanHandle<T>::new,
                ha -> installationAction.accept(ha.configuration()));
        return h.configuration();
    }


    /**
     * Creates a new bean installer to be able to install a new bean on behalf of the user.
     *
     * @param template
     *            a bean template representing the behaviour of the new bean
     * @return the installer
     */
    public BeanInstaller newBean(BeanKind bl) {
        ExtensionSetup e = handle.usedBy();
        return PackedBeanInstaller.newInstaller(bl, e, e.container.assembly);
    }

    public BeanInstaller newBean(BeanKind bl, ExtensionPointHandle forExtension) {
        requireNonNull(forExtension, "forExtension is null");
        return PackedBeanInstaller.newInstaller(bl, extension().extension, ((PackedExtensionPointHandle) forExtension).usedBy());
    }

    /**
     * Creates a new container installer to be able to install a new container on behalf of the user.
     *
     * @param containerKind
     *            the kind of container to create
     * @return the installer
     */
    ContainerInstaller<?> newContainer() {
        // Kan only use channels that are direct dependencies of the usage extension
        ExtensionSetup es = handle.usedBy();
        return PackedContainerInstaller.of(es.extensionType, es.container.application, es.container);
    }

    public int registerEntryPoint(Class<?> hook) {
        return handle.extension().container.lifetime.entryPoints.takeOver(extension(), usedBy());// .registerEntryPoint(usedBy(), isMain);
    }

    private static OldContainerTemplateLink.Configurator baseBuilder(String name) {
        return OldContainerTemplateLink.of(MethodHandles.lookup(), BaseExtension.class, name);
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

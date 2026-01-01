package app.packed.extension;

import java.util.function.Consumer;

import app.packed.assembly.Assembly;
import app.packed.bean.Bean;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanTemplate;
import app.packed.bean.sandbox.BeanSynthesizer;
import app.packed.build.action.BuildActionable;
import app.packed.container.ContainerBuildLocal;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerInstaller;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.extension.ExtensionPoint.ExtensionPointHandle;
import app.packed.operation.Op;
import app.packed.service.ProvidableBeanConfiguration;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceNamespaceConfiguration;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.bean.PackedBeanInstaller.ProvidableBeanHandle;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.service.util.PackedServiceLocator;

/**
 * An extension that defines the foundational APIs for managing beans, services, containers and applications.
 * <p>
 * Every container automatically uses this extension. And every extension automatically has a direct dependency on this
 * extension.
 * <p>
 * All methods on this class deals with beans Table area [bean,container,service] prefix desciption
 * <p>
 * This extension does not define an {@link ExtensionExtension extension mirror}. Instead all relevant methods are
 * placed directly on {@link app.packed.bean.BeanMirror}, {@link app.packed.container.ContainerMirror},
 * {@link app.packed.application.ApplicationMirror} and relevant {@link app.packed.namespace.NamespaceMirror}s.
 *
 * @see app.packed.container.BaseAssembly#base()
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

    static final BeanTemplate DEFAULT_BEAN = PackedBeanTemplate.builder(BeanLifetime.SINGLETON)
            .initialization(PackedOperationTemplate.DEFAULTS.withReturnTypeDynamic())
            .build();

    /**
     * All your base are belong to us.
     *
     * @param handle
     *            the extension's handle
     */
    BaseExtension(ExtensionHandle<BaseExtension> handle) {
        super(handle);
    }

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

    // One of 3 models...
    // Fails on other exports
    // Ignores other exports
    // interacts with other exports in some way


    public <T> ProvidableBeanConfiguration<T> install(Bean<T> bean) {
        BeanHandle<ProvidableBeanConfiguration<T>> h = install0(DEFAULT_BEAN).install(bean, ProvidableBeanHandle::new);
        return h.configuration();
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
        return install(Bean.of(implementation));
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
        return install(Bean.of(op));
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
        return install(Bean.ofInstance(instance));
    }

    public <T> ProvidableBeanConfiguration<T> installPrototype(Bean<T> bean) {
        BeanHandle<ProvidableBeanConfiguration<T>> handle = install0(BeanLifetime.UNMANAGED.template()).install(bean, ProvidableBeanHandle::new);
        return handle.configuration();
    }

    // Kan ikke se den her giver mening andet end som provide();
    public <T> ProvidableBeanConfiguration<T> installPrototype(Class<T> implementation) {
        return installPrototype(Bean.of(implementation));
    }

    public <T> ProvidableBeanConfiguration<T> installPrototype(Op<T> op) {
        return installPrototype(Bean.of(op));
    }

//    public <T> BeanConfiguration installStatic(Bean<T> bean) {
//        BeanHandle<BeanConfiguration> handle = install0(BeanKind.STATIC.template()).install(bean, BeanHandle::new);
//        return handle.configuration();
//    }
//
//    /**
//     * Installs a new {@link BeanKind#STATIC static} bean.
//     *
//     * @param implementation
//     *            the static bean class
//     * @return a configuration for the bean
//     *
//     * @see BeanKind#STATIC
//     * @see BeanSourceKind#CLASS
//     */
//    public BeanConfiguration installStatic(Class<?> implementation) {
//        return installStatic(Bean.of(implementation));
//    }

    /**
     * Installs an exported service locator.
     *
     * @see BaseExtensionPoint#EXPORTED_SERVICE_LOCATOR
     */
    void lifetimeExportServiceLocator() {
        // Create a new bean that holds the ServiceLocator to export
        // will fail if installed multiple times

        BeanHandle<ProvidableBeanConfiguration<PackedServiceLocator>> ha = newBeanBuilderSelf(DEFAULT_BEAN).install(Bean.of(PackedServiceLocator.class),
                ProvidableBeanHandle::new);
        ha.configuration().exportAs(ServiceLocator.class);

        // PackedServiceLocator needs a Map<Key, MethodHandle> which is created in the code generation phase
        ha.bindComputedConstant(PackedServiceLocator.KEY, () -> extension.container.servicesMain().exportedServices());

        // Alternative, If we do not use it for anything else
        newBeanBuilderSelf(DEFAULT_BEAN).installIfAbsent(PackedServiceLocator.class, BeanHandle.class, BeanHandle::new, bh -> {
            bh.exportAs(ServiceLocator.class);
            bh.bindComputedConstant(PackedServiceLocator.KEY, () -> extension.container.servicesMain().exportedServices());
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
    // Maaske have en Container Linked???
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
    private ContainerInstaller<?> link0() {
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

    /** {@return a mirror for this extension.} */
    @Override
    protected BaseExtensionMirror newExtensionMirror() {
        return new BaseExtensionMirror(handle());
    }

    /** {@inheritDoc} */
    @Override
    protected BaseExtensionPoint newExtensionPoint(ExtensionPointHandle usesite) {
        return new BaseExtensionPoint(usesite);
    }

    /**
     * {@inheritDoc}
     * <p>
     * BaseExtension is always the last extension to be closed. As it is the only extension that has
     * {@link internal.app.packed.container.ExtensionModel#orderingDepth()} 0.
     */
    @Override
    protected void onConfigured() {
        // close child extensions first
        super.onConfigured();

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
    @SuppressWarnings("exports")
    public Runnable transformAllBeans(Consumer<? super BeanSynthesizer> transformer) {
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
    @SuppressWarnings("exports")
    public Runnable transformBeans(Consumer<? super BeanSynthesizer> transformer) {
        throw new UnsupportedOperationException();
    }

    // Har man brug for andet end class transformer? Er der nogle generalle bean properties??? IDK
    <T> ProvidableBeanConfiguration<T> transformingInstall(Class<T> implementation, Consumer<? super BeanSynthesizer> transformation) {
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

    @SuppressWarnings("exports")
    public void transformNextBean(Consumer<? super BeanSynthesizer> transformer) {}

    static class FromLinks {
        boolean exportServices;
    }
}

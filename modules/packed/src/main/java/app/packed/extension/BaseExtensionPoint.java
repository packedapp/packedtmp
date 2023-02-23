package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.extension.BeanHook.AnnotatedBindingHook;
import app.packed.extension.bean.BeanBuilder;
import app.packed.extension.bean.BeanHandle;
import app.packed.extension.bean.BeanTemplate;
import app.packed.extension.container.ContainerBuilder;
import app.packed.extension.container.ContainerHolderConfiguration;
import app.packed.extension.container.ContainerTemplate;
import app.packed.extension.container.ExtensionLink;
import app.packed.extension.operation.DelegatingOperationHandle;
import app.packed.extension.operation.OperationHandle;
import app.packed.extension.operation.OperationTemplate;
import app.packed.lifetime.LifetimeOrder;
import app.packed.lifetime.RunState;
import app.packed.lifetime.sandbox.ManagedLifetimeController;
import app.packed.operation.Op;
import app.packed.operation.OperationConfiguration;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.Key;
import internal.app.packed.bean.BeanLifecycleOrder;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanBuilder;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedContainerBuilder;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.PackedOperationHandle;

/** An {@link ExtensionPoint extension point} for {@link BaseExtension}. */
public class BaseExtensionPoint extends ExtensionPoint<BaseExtension> {

    // Hmm Installering af MirrorExtension er lidt hmm hmm

    // Maaske er det bare en dum extension?
    // Og saa paa en anden checke om der er mirrors
    // Alle mirror typerne er jo en del af base's ansvar

    // Altsaa fx naar vi naar hen til application.
    // Vi kan jo ikke installere den i extensionen...
    public static ExtensionLink CONTAINER_MIRROR = ExtensionLink.builder(MethodHandles.lookup(), BaseExtension.class, "ContainerMirror").build();

    /** A bridge that makes the name of the container available. */
    public static final ExtensionLink CONTAINER_NAME = null;

    /**
     * A container lifetime channel that makes the container's exported services available as
     * {@link app.packed.service.ServiceLocator}.
     */
    public static final ExtensionLink EXPORTED_SERVICE_LOCATOR = baseBuilder("ExportedServiceLocator")
            .consumeLocal(BaseExtension.FROM_LINKS, t -> t.exportServices = true).expose(ServiceLocator.class).build();

    // Teanker vi altid exportere den
    // check that we have a managed lifetime. Maybe PackedManagedBeanController is already installed
    // baseExtension.managedLifetimeBean.export(); // maybe it is already exported

    public static final ExtensionLink MANAGED_LIFETIME_CONTROLLER = baseBuilder("ManagedLifetimeController").expose(ManagedLifetimeController.class).build();

    /** Creates a new base extension point. */
    BaseExtensionPoint() {}

    // Alternativt tager vi ikke en bean. Men en container som er implicit
    // Det betyder nu ogsaa at CodeGenerated er for hele containeren og ikke bare en bean.
    // Maaske supportere begge ting?
    // Det eneste jeg kunne forstille mig at man ikke ville container wide var hvis man havde en bean
    // per X. Men taenker men saa har et arrays
    public <K> void addCodeGenerated(BeanConfiguration bean, Class<K> key, Supplier<? extends K> supplier) {
        addCodeGenerated(bean, Key.of(key), supplier);
    }

    /**
     * Registers a code generating supplier that can be used together with {@link CodeGenerated} annotation.
     *
     * <p>
     * Internally this mechanisms uses
     *
     * @param <K>
     *            the type of value the supplier produces
     * @param bean
     *            the bean to bind to
     * @param key
     *            the type of key used together with {@link CodeGenerated}
     * @param supplier
     *            the supplier generating the value
     *
     * @throws IllegalArgumentException
     *             if the specified bean is not owned by this extension. Or if the specified bean is not part of the same
     *             container as this extension. Or if the specified bean does not have an injection site matching the
     *             specified key.
     * @throws IllegalStateException
     *             if a supplier has already been registered for the specified key in the same container, or if the
     *             extension is no longer configurable.
     * @see CodeGenerated
     * @see BindableVariable#bindGeneratedConstant(Supplier)
     */
    public <K> void addCodeGenerated(BeanConfiguration bean, Key<K> key, Supplier<? extends K> supplier) {
        requireNonNull(bean, "bean is null");
        requireNonNull(key, "key is null");
        requireNonNull(supplier, "supplier is null");
        checkIsConfigurable();

        BeanSetup b = BeanSetup.crack(bean);
        BaseExtension be = extension();

        if (!bean.owner().isExtension(usedBy())) {
            throw new IllegalArgumentException("Bean Owner " + bean.owner() + " ");
        } else if (b.container != be.extension.container) {
            throw new IllegalArgumentException(); // Hmm? maybe allow it
        }

        be.addCodeGenerated(b, key, supplier);
    }

    /**
     * Creates a new application bean installer.
     *
     * @param template
     *            a template for the bean's lifetime
     * @return the installer
     */
    public BeanBuilder beanBuilder(BeanTemplate template) {
        return new PackedBeanBuilder(extension().extension, extension().extension.container.assembly, template);
    }

    /**
     * Creates a new extension bean installer.
     *
     * @param template
     *            a template for the bean's lifetime
     * @return the installer
     */
    public BeanBuilder beanInstallerForExtension(BeanTemplate template, UseSite forExtension) {
        requireNonNull(forExtension, "forExtension is null");
        return new PackedBeanBuilder(extension().extension, ((PackedExtensionPointContext) forExtension).usedBy(), template);
    }

    /**
     * Create a new container builder using the specified container template.
     *
     * @param template
     *            the container's template
     * @return a new container builder
     */
    public ContainerBuilder containerBuilder(ContainerTemplate template) {
        // Kan only use channels that are direct dependencies of the usage extension
        ExtensionSetup es = contextUse().usedBy();
        return PackedContainerBuilder.of(template, es.extensionType, es.container.application, es.container);
    }

//    // Contexts
//    public ContainerBuilder containerInstallerExistingLifetime(boolean isLazy) {
//        // Kan only use channels that are direct dependencies of the usage extension
//
//        ExtensionSetup s = contextUse().usedBy();
//        return new PackedContainerBuilder(ContainerTemplate.IN_PARENT, s.extensionType, s.container.application, s.container);
//    }

    // Can I come up with a situation where we want multiple guests of the same type??
    // I think not
    public <T> ContainerHolderConfiguration<T> containerHolderInstall(Class<T> holderClass, boolean allowMulti) {
        throw new UnsupportedOperationException();
    }

    public <T> ContainerHolderConfiguration<T> containerHolderInstallIfAbsent(Class<T> holderClass, Consumer<? super ContainerHolderConfiguration<T>> action) {
        throw new UnsupportedOperationException();
    }

    public <T> ServiceableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = beanInstallerForExtension(BeanTemplate.CONTAINER, context()).install(implementation);
        return new ServiceableBeanConfiguration<>(handle);
    }

    /**
     * @param <T>
     *            the type of bean to install
     * @param op
     *            an operation responsible for creating an instance of the bean when the container is initialized
     * @return a configuration object representing the installed bean
     */
    public <T> InstanceBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<T> handle = beanInstallerForExtension(BeanTemplate.CONTAINER, context()).install(op);
        return new InstanceBeanConfiguration<>(handle);
    }

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
    public <T> InstanceBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super InstanceBeanConfiguration<T>> action) {
        requireNonNull(action, "action is null");
        BeanHandle<T> handle = beanInstallerForExtension(BeanTemplate.CONTAINER, context()).installIfAbsent(clazz,
                h -> action.accept(new InstanceBeanConfiguration<>(h)));
        return new InstanceBeanConfiguration<>(handle);
    }

    public <T> InstanceBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = beanInstallerForExtension(BeanTemplate.CONTAINER, context()).installInstance(instance);
        return new InstanceBeanConfiguration<>(handle);
    }

    /**
     * Installs a {@link BeanKind#STATIC static} bean.
     *
     * @param beanClass
     *            the type of static bean to install
     * @return a configuration object representing the installed bean
     */
    public BeanConfiguration installStatic(Class<?> beanClass) {
        BeanHandle<?> handle = beanInstallerForExtension(BeanTemplate.STATIC, context()).install(beanClass);
        return new BeanConfiguration(handle);
    }

    public FunctionalBeanConfiguration installFunctional() {
        PackedBeanBuilder bb = (PackedBeanBuilder) beanInstallerForExtension(BeanTemplate.STATIC, context());
        BeanHandle<?> handle = bb.installSourceless();
        return new FunctionalBeanConfiguration(handle);
    }

    public OperationConfiguration runOnBean(RunState state, DelegatingOperationHandle h, LifetimeOrder ordering) {
        throw new UnsupportedOperationException();
    }

//    // onExtension E newContainer(Wirelet wirelets); // adds this to the container and returns it
//
//    public ContainerHandle newContainer(Assembly assembly, Wirelet... wirelets) {
//        throw new UnsupportedOperationException();
//    }
//
//    public ContainerHandle newContainer(Wirelet... wirelets) {
//        // What is the usecase here without
//        // Okay I want to a create a container in the container.
//        // And then add myself
//
//        // Let's say entity beans are always in their own container
//        // newContainer().useMyself().
//
//        throw new UnsupportedOperationException();
//    }
//
//    public <T> ContainerGuestBeanConfiguration<T> newContainerGuest(Class<T> containerGuest, ExtensionLifetimeBridge... bridges) {
//        throw new UnsupportedOperationException();
//    }

    public OperationConfiguration runOnBeanInitialization(DelegatingOperationHandle h, LifetimeOrder ordering) {
        requireNonNull(ordering, "ordering is null");
        OperationHandle handle = h.newOperation(OperationTemplate.defaults(), context());
        ((PackedOperationHandle) handle).operation().bean.addLifecycleOperation(BeanLifecycleOrder.fromInitialize(ordering), handle);
        return new OperationConfiguration(handle);
    }

    /**
     * Creates a new inject operation from the specified delegating operation handle.
     *
     * @param h
     *            the delegating handle that the operation should be created from.
     * @return a configuration object representing the inject operation
     * @see Inject
     */
    public OperationConfiguration runOnBeanInject(DelegatingOperationHandle h) {
        PackedOperationHandle handle = (PackedOperationHandle) h.newOperation(OperationTemplate.defaults(), context());
        handle.operation().bean.addLifecycleOperation(BeanLifecycleOrder.INJECT, handle);
        return new OperationConfiguration(handle);
    }

    public OperationConfiguration runOnBeanStart(DelegatingOperationHandle h, LifetimeOrder ordering) {
        // What if I want to fork it??? on OC??
        // Or do I need to call it immediately
        // runOnLifecycle(RunState runstate, LifecycleOrder ordering)
        throw new UnsupportedOperationException();
    }

    public OperationConfiguration runOnBeanStop(DelegatingOperationHandle h, LifetimeOrder ordering) {
        throw new UnsupportedOperationException();
    }

    private static ExtensionLink.Builder baseBuilder(String name) {
        return ExtensionLink.builder(MethodHandles.lookup(), BaseExtension.class, name);
    }

    /**
     * This annotation is used to indicate that the annotated variable is constructed doing the code generation phase of the
     * application.
     * <p>
     * This annotation can only used by extensions.
     *
     * @see BindableVariable#bindGeneratedConstant(java.util.function.Supplier)
     * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, Class, java.util.function.Supplier)
     * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, app.packed.bindings.Key,
     *      java.util.function.Supplier)
     */
    @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedBindingHook(extension = BaseExtension.class)
    public @interface CodeGenerated {}
}

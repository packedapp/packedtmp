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
import app.packed.bean.BeanKind;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.extension.BeanClassActivator.AnnotatedBeanVariableActivator;
import app.packed.lifetime.RunState;
import app.packed.operation.Op;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationDependencyOrder;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.Key;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanHandleBuilder;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.NonRootContainerBuilder;
import internal.app.packed.container.PackedExtensionPointContext;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.bean.BeanTemplate;
import sandbox.extension.container.ContainerCarrierBeanConfiguration;
import sandbox.extension.container.ContainerHandle;
import sandbox.extension.container.ContainerTemplate;
import sandbox.extension.container.ContainerTemplatePack;
import sandbox.extension.operation.OperationHandle.Builder;
import sandbox.lifetime.external.LifecycleController;

/** An {@link ExtensionPoint extension point} for {@link BaseExtension}. */
public class BaseExtensionPoint extends ExtensionPoint<BaseExtension> {

    // Altsaa fx naar vi naar hen til application.
    // Vi kan jo ikke installere den i extensionen...
    public static ContainerTemplatePack CONTAINER_MIRROR = ContainerTemplatePack.builder(MethodHandles.lookup(), BaseExtension.class, "ContainerMirror")
            .build();

    /** A bridge that makes the name of the container available. */
    public static final ContainerTemplatePack CONTAINER_NAME = null;

    /**
     * A container lifetime channel that makes the container's exported services available as
     * {@link app.packed.service.ServiceLocator}.
     */
    public static final ContainerTemplatePack EXPORTED_SERVICE_LOCATOR = baseBuilder("ExportedServiceLocator")
            .localConsume(BaseExtension.FROM_LINKS, t -> t.exportServices = true).provideExpose(ServiceLocator.class).build();

    // Teanker vi altid exportere den
    // check that we have a managed lifetime. Maybe PackedManagedBeanController is already installed
    // baseExtension.managedLifetimeBean.export(); // maybe it is already exported

    public static final ContainerTemplatePack MANAGED_LIFETIME_CONTROLLER = baseBuilder("ManagedLifetimeController").provideExpose(LifecycleController.class)
            .build();

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
     * Registers a code generating supplier whose supplied value can be consumed by a variable annotated with
     * {@link CodeGenerated}.
     * <p>
     * Internally this mechanisms uses
     *
     * @param <K>
     *            the type of value the supplier produces
     * @param bean
     *            the bean to bind the supplier to
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

        if (!bean.author().isExtension(usedBy())) {
            throw new IllegalArgumentException("Bean Owner " + bean.author() + " ");
        } else if (b.container != be.extension.container) {
            throw new IllegalArgumentException(); // Hmm? maybe allow it
        }

        be.addCodeGenerated(b, key, supplier);
    }

    public <T> ServiceableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = newBeanForOtherExtension(BeanKind.CONTAINER.template(), context()).install(implementation);
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
        BeanHandle<T> handle = newBeanForOtherExtension(BeanKind.CONTAINER.template(), context()).install(op);
        // return handle.initialize(IBC::new);
        return new InstanceBeanConfiguration<>(handle);
    }

    // Can I come up with a situation where we want multiple guests of the same type??
    // I think not
    public <T> ContainerCarrierBeanConfiguration<T> installContainerCarrier(Class<T> holderClass) {
        throw new UnsupportedOperationException();
    }

    public <T> ContainerCarrierBeanConfiguration<T> installContainerCarrier(Op<T> holderClass) {
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
        BeanHandle<T> handle = newBeanForOtherExtension(BeanKind.CONTAINER.template(), context()).installIfAbsent(clazz,
                h -> action.accept(new InstanceBeanConfiguration<>(h)));
        BeanConfiguration bc = BeanSetup.crack(handle).configuration;
        if (bc == null) {
            return new InstanceBeanConfiguration<>(handle);
        } else if (bc instanceof InstanceBeanConfiguration<?> ibc) {
            return (InstanceBeanConfiguration<T>) ibc;
        } else {
            throw new IllegalStateException();
        }
    }

    public <T> InstanceBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = newBeanForOtherExtension(BeanKind.CONTAINER.template(), context()).installInstance(instance);
        return new InstanceBeanConfiguration<>(handle);
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
        BeanHandle<?> handle = newBeanForOtherExtension(BeanKind.STATIC.template(), context()).install(beanClass);
        return new BeanConfiguration(handle);
    }

    /**
     * Creates a new extension bean installer.
     *
     * @param template
     *            a template for the bean's lifetime
     * @return the installer
     */
    public BeanHandle.Builder newBeanForOtherExtension(BeanTemplate template, UseSite forExtension) {
        requireNonNull(forExtension, "forExtension is null");
        return new PackedBeanHandleBuilder(extension().extension, ((PackedExtensionPointContext) forExtension).usedBy(), template);
    }

    /**
     * Creates a new application bean installer.
     *
     * @param template
     *            a template for the bean's lifetime
     * @return the installer
     */
    public BeanHandle.Builder newBeanForUser(BeanTemplate template) {
        return new PackedBeanHandleBuilder(extension().extension, extension().extension.container.assembly, template);
    }

    /**
     * Create a new container builder using the specified container template.
     *
     * @param template
     *            the container's template
     * @return a new container builder
     */
    public ContainerHandle.Builder newContainer(ContainerTemplate template) {
        // Kan only use channels that are direct dependencies of the usage extension
        ExtensionSetup es = contextUse().usedBy();
        return NonRootContainerBuilder.of(template, es.extensionType, es.container.application, es.container);
    }

    public int registerEntryPoint(Class<?> hook) {
        return super.extensionSetup().container.lifetime.entryPoints.takeOver(extension(), usedBy());// .registerEntryPoint(usedBy(), isMain);
    }

    public OperationConfiguration runLifecycleOperation(Builder operation, RunState state, OperationDependencyOrder ordering) {
        throw new UnsupportedOperationException();
    }

    public OperationConfiguration runOnBeanInitialization(Builder operation, OperationDependencyOrder ordering) {
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
    public OperationConfiguration runOnBeanInject(Builder operation) {
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

    private static ContainerTemplatePack.Builder baseBuilder(String name) {
        return ContainerTemplatePack.builder(MethodHandles.lookup(), BaseExtension.class, name);
    }

    /**
     * This annotation is used to indicate that the value of a annotated variable (field or parameter) of a bean is
     * constructed doing the code generation phase of the application.
     * <p>
     * Values for a specific bean must be provided either via {@link BaseExtensionPoint}
     *
     * <p>
     * This annotation can only be used by extensions.
     *
     * @see BindableVariable#bindGeneratedConstant(java.util.function.Supplier)
     * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, Class, java.util.function.Supplier)
     * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, app.packed.bindings.Key,
     *      java.util.function.Supplier)
     */
    @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedBeanVariableActivator(extension = BaseExtension.class)
    public @interface CodeGenerated {}
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
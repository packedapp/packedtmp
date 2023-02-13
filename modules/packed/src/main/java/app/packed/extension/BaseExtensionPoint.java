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
import app.packed.bean.BeanMirror;
import app.packed.bean.DependencyOrder;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.extension.BeanHook.AnnotatedBindingHook;
import app.packed.lifetime.RunState;
import app.packed.lifetime.sandbox.ManagedLifetimeController;
import app.packed.operation.Op;
import app.packed.operation.OperationConfiguration;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.Key;
import internal.app.packed.bean.BeanLifecycleOrder;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.PackedOperationHandle;

/** An {@link ExtensionPoint extension point} for {@link BaseExtension}. */
public class BaseExtensionPoint extends ExtensionPoint<BaseExtension> {

    /** A bridge that makes the name of the container available. */
    public static final ContainerLifetimeChannel CONTAINER_NAME = null;

    /**
     * A container lifetime channel that makes the container's exported services available as
     * {@link app.packed.service.ServiceLocator}.
     */
    public static final ContainerLifetimeChannel EXPORTED_SERVICE_LOCATOR = baseBuilder("ExportedServiceLocator")
            .onUse(BaseExtension::lifetimeExportServiceLocator).expose(ServiceLocator.class).build();

    // Teanker vi altid exportere den
    public static final ContainerLifetimeChannel MANAGED_LIFETIME_CONTROLLER = baseBuilder("ManagedLifetimeController").onUse(e -> {
        // check that we have a managed lifetime. Maybe PackedManagedBeanController is already installed
        // baseExtension.managedLifetimeBean.export(); // maybe it is already exported
    }).expose(ManagedLifetimeController.class).build();

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
    public BeanInstaller beanInstaller(BeanLifetimeTemplate template) {
        return new PackedBeanInstaller(extension().extension, extension().extension.container.assembly, template);
    }

    /**
     * Creates a new extension bean installer.
     *
     * @param template
     *            a template for the bean's lifetime
     * @return the installer
     */
    public BeanInstaller beanInstallerForExtension(BeanLifetimeTemplate template, UseSite forExtension) {
        requireNonNull(forExtension, "forExtension is null");
        return new PackedBeanInstaller(extension().extension, ((PackedExtensionPointContext) forExtension).usedBy(), template);
    }

    public ContainerInstaller containerInstaller(ContainerLifetimeTemplate template) {
        // Kan only use channels that are direct dependencies of the usage extension
        ExtensionSetup s = extension().extension;
        return new PackedContainerInstaller(template, s.extensionType, s.container.application, s.container);
    }

    // Contexts
    public ContainerInstaller containerInstallerExistingLifetime(boolean isLazy) {
        // Kan only use channels that are direct dependencies of the usage extension
        ExtensionSetup s = extension().extension;
        return new PackedContainerInstaller(ContainerLifetimeTemplate.PARENT, s.extensionType, s.container.application, s.container);
    }

    public <T> InstanceBeanConfiguration<T> containerInstallHost(Class<T> hostClass) {
        throw new UnsupportedOperationException();
    }

    public BeanHandle<?> crack(BeanConfiguration configuration) {
        throw new UnsupportedOperationException();
    }

    public <T> ServiceableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = beanInstallerForExtension(BeanLifetimeTemplate.CONTAINER, context()).install(implementation);
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
        BeanHandle<T> handle = beanInstallerForExtension(BeanLifetimeTemplate.CONTAINER, context()).install(op);
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
        BeanHandle<T> handle = beanInstallerForExtension(BeanLifetimeTemplate.CONTAINER, context()).installIfAbsent(clazz,
                h -> action.accept(new InstanceBeanConfiguration<>(h)));
        return new InstanceBeanConfiguration<>(handle);
    }

    public <T> InstanceBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = beanInstallerForExtension(BeanLifetimeTemplate.CONTAINER, context()).installInstance(instance);
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
        BeanHandle<?> handle = beanInstallerForExtension(BeanLifetimeTemplate.STATIC, context()).install(beanClass);
        return new BeanConfiguration(handle);
    }

    public OperationConfiguration runOnBean(RunState state, DelegatingOperationHandle h, DependencyOrder ordering) {
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

    public OperationConfiguration runOnBeanInitialization(DelegatingOperationHandle h, DependencyOrder ordering) {
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

    public OperationConfiguration runOnBeanStart(DelegatingOperationHandle h, DependencyOrder ordering) {
        // What if I want to fork it??? on OC??
        // Or do I need to call it immediately
        // runOnLifecycle(RunState runstate, LifecycleOrder ordering)
        throw new UnsupportedOperationException();
    }

    public OperationConfiguration runOnBeanStop(DelegatingOperationHandle h, DependencyOrder ordering) {
        throw new UnsupportedOperationException();
    }

    private static ContainerLifetimeChannel.Builder<BaseExtension> baseBuilder(String name) {
        return ContainerLifetimeChannel.builder(MethodHandles.lookup(), BaseExtension.class, name);
    }

    /**
     * An installer for installing beans into a container.
     * <p>
     * The various install methods can be called multiple times to install multiple beans. However, the use cases for this
     * are limited.
     *
     * @see BaseExtensionPoint#newBean(BeanKind)
     * @see BaseExtensionPoint#newBeanForExtension(BeanKind, app.packed.extension.ExtensionPoint.UseSite)
     */
// Maybe put it back on handle. If we get OperationInstaller
// Maybe Builder after all... Alle ved hvad en builder er
    public sealed interface BeanInstaller permits PackedBeanInstaller {

        /**
         * Installs the bean using the specified class as the bean source.
         *
         * @param <T>
         *            the
         * @param beanClass
         * @return a bean handle representing the installed bean
         */
        <T> BeanHandle<T> install(Class<T> beanClass);

        <T> BeanHandle<T> install(Op<T> operation);

        <T> BeanHandle<T> installIfAbsent(Class<T> beanClass, Consumer<? super BeanHandle<T>> onInstall);

        <T> BeanHandle<T> installInstance(T instance);

        BeanHandle<Void> installWithoutSource();

        /**
         * An option that allows for a special bean introspector to be used when introspecting the bean for the extension.
         * Normally, the runtime would call {@link Extension#newBeanIntrospector} to obtain an introspector for the registering
         * extension.
         *
         * @param introspector
         *            the introspector to use
         * @return the option
         * @see Extension#newBeanIntrospector
         */
        // Den er langt mindre brugbar end foerst antaget. Fordi vi bliver noedt til at processere alle
        // annotering og give gode fejlmeddelse for hvorfor man ikke kan benytte dem

        // Hvad skal vi helt praecis goere her...
        // Vi bliver noedt til at vide hvilke kontekts der er...
        // Saa vi skal vel have OperationTemplates

        //// Hvad med @Get som laver en bean...
        //// Det er vel operationen der laver den...

        // No Lifetime, Container, Static, Functional, Static

        // Operational -> A bean that is instantiated and lives for the duration of an operation

        // MANYTONE -> Controlled

        /**
         * Allows multiple beans of the same type in a container.
         * <p>
         * By default, a container only allows a single bean of particular type if non-void.
         *
         * @return this builder
         * @throws UnsupportedOperationException
         *             if bean kind is {@link BeanKind#FUNCTIONAL} or {@link BeanKind#STATIC}
         */
        BeanInstaller multi();

        BeanInstaller namePrefix(String prefix);

        <T> BeanInstaller setLocal(BeanLocal<T> local, T value);

        // A bean that is created per operation.
        // Obvious manyton, but should we have own kind?
        // I actually think so because, because for now it always requires manyton

        // Some questions, do we support @Schedule? Or anything like it?
        // I don't think we need to set up the support for it by default. Only if used
        // So overhead is not needed

        // But I think those annotations that make sense are always "callback" extensions
        // From other threads
        // Single threaded vs multi-threaded
        // If we are single threaded it is obviously always only the request method
        // If we are multi threaded we create own little "world"
        // I think that is the difference, between the two

        // Maybe bean is always single threaded.
        // And container is always multi threaded

        /**
         * Sets a supplier that creates a special bean mirror instead of the generic {@code BeanMirror} when requested.
         *
         * @param supplier
         *            the supplier used to create the bean mirror
         * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
         *          must be returned
         */
        BeanInstaller specializeMirror(Supplier<? extends BeanMirror> supplier);

        /**
         * Marks the bean as synthetic.
         *
         * @return this installer
         */
        BeanInstaller synthetic();
    }

    /**
     * This annotation is used to indicate that the variable is constructed doing the code generation phase of the
     * application.
     * <p>
     * Man kan selvfoelgelig kun bruge den paa
     *
     * <p>
     * This annotation can only used by beans owned by an extension.
     *
     * @see BindableVariable#bindGeneratedConstant(java.util.function.Supplier)
     * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, Class, java.util.function.Supplier)
     * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, app.packed.bindings.Key,
     *      java.util.function.Supplier)
     */
    @Target({ ElementType.PARAMETER, ElementType.TYPE_USE })
    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedBindingHook(extension = BaseExtension.class)
    public @interface CodeGenerated {}
}

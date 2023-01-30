package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHook.AnnotatedBindingHook;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.Inject;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.bean.LifecycleOrdering;
import app.packed.bindings.BindableVariable;
import app.packed.bindings.Key;
import app.packed.container.Assembly;
import app.packed.container.ContainerHandle;
import app.packed.container.Wirelet;
import app.packed.operation.DelegatingOperationHandle;
import app.packed.operation.Op;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.PackedOperationHandle;

/** An {@link ExtensionPoint extension point} class for {@link BaseExtension}. */
public class BaseExtensionPoint extends ExtensionPoint<BaseExtension> {

    /** Creates a new base extension point. */
    BaseExtensionPoint() {}

    // Alternativt tager vi ikke en bean. Men en container som er implicit
    
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

    public <T> InstanceBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = newBeanForExtension(BeanKind.CONTAINER, context()).install(implementation);
        return new InstanceBeanConfiguration<>(handle);
    }

    /**
     * @param <T>
     *            the type of bean to install
     * @param op
     *            an operation responsible for creating an instance of the bean when the container is initialized
     * @return a configuration object representing the installed bean
     */
    public <T> InstanceBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<T> handle = newBeanForExtension(BeanKind.CONTAINER, context()).install(op);
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
        BeanHandle<T> handle = newBeanForExtension(BeanKind.CONTAINER, context()).installIfAbsent(clazz,
                h -> action.accept(new InstanceBeanConfiguration<>(h)));
        return new InstanceBeanConfiguration<>(handle);
    }

    public <T> InstanceBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = newBeanForExtension(BeanKind.CONTAINER, context()).installInstance(instance);
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
        BeanHandle<?> handle = newBeanForExtension(BeanKind.STATIC, context()).install(beanClass);
        return new BeanConfiguration(handle);
    }

    /**
     * Creates a new bean installer for the application.
     * 
     * @param kind
     *            the kind of bean to installer
     * @return the installer
     */
    public BeanInstaller newBean(BeanKind kind) {
        return new PackedBeanInstaller(extension().extension, kind, (PackedExtensionPointContext) context());
    }

    /**
     * Creates a new bean installer for an extension.
     * 
     * @param kind
     *            the kind of bean to installer
     * @return the installer
     */
    public BeanInstaller newBeanForExtension(BeanKind kind, UseSite forExtension) {
        requireNonNull(forExtension, "forExtension is null");
        return new PackedBeanInstaller(extension().extension, kind, (PackedExtensionPointContext) forExtension);
    }

    public ContainerInstaller newContainer() {
        throw new UnsupportedOperationException();
    }

    public OperationConfiguration runOnBeanInitialization(DelegatingOperationHandle h, LifecycleOrdering ordering) {
        requireNonNull(ordering, "ordering is null");
        OperationHandle handle = h.newOperation(context(), OperationTemplate.defaults());
        ((PackedOperationHandle) handle).operation().bean.lifecycle.addInitialize(handle, ordering);
        return new OperationConfiguration(handle);
    }

    /**
     * Creates a new inject operation from the specified handle.
     * 
     * @param handle
     *            the operation that should be executed as part of its bean's injection phase
     * @return a configuration object
     * @see Inject
     */
    public OperationConfiguration runOnBeanInject(DelegatingOperationHandle h) {
        OperationHandle handle = h.newOperation(context(), OperationTemplate.defaults());
        ((PackedOperationHandle) handle).operation().bean.lifecycle.addInitialize(handle, null);
        return new OperationConfiguration(handle);
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

        // can be used for inter
        // Maybe use ScopedValues instead???
        <A> BeanInstaller attach(Class<A> attachmentType, A attachment);

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
        BeanInstaller introspectWith(BeanIntrospector introspector);

        // Hvad skal vi helt praecis goere her...
        // Vi bliver noedt til at vide hvilke kontekts der er...
        // Saa vi skal vel have OperationTemplates

        //// Hvad med @Get som laver en bean...
        //// Det er vel operationen der laver den...

        // No Lifetime, Container, Static, Functional, Static

        // Operational -> A bean that is instantiated and lives for the duration of an operation

        // MANYTONE -> Controlled

        default BeanInstaller lifetimeFromOperations() {
            return this;
        }

        BeanInstaller lifetimes(OperationTemplate... templates);

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

        default BeanInstaller spawnNew() {
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

            throw new UnsupportedOperationException();
        }

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
     * This annotation can only used by extension beans.
     * 
     * @see BindableVariable#bindGeneratedConstant(java.util.function.Supplier)
     * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, Class, java.util.function.Supplier)
     * @see BaseExtensionPoint#addCodeGenerated(app.packed.bean.BeanConfiguration, app.packed.bindings.Key,
     *      java.util.function.Supplier)
     */
    @Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE })
    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedBindingHook(extension = BaseExtension.class)
    public @interface CodeGenerated {}

    // Vi har brug ContainerInstaller fordi, man ikke konfigure noget efter man har linket
    // Saa alt skal goeres inde

    // Bliver noedt til at lave et Handle. Da kalderen som minim har brug for
    // OperationHandles for lifetimen...

    // Ejer

    // Support enten linkage(Assembly) or lav en ny XContetainerConfiguration
    // Eager, Lazy, ManyTone
    // ContainerCompanions (extension configuration)
    // Bean <- er taet knyttet til ContainerCompanions
    // Hosting (Long term)

    // Lifetime -> In Operation, Start/Stop, stateless?

    public interface ContainerInstaller {

        ContainerInstaller allowRuntimeWirelets();

        /**
         * <p>
         * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}
         * 
         * @param assembly
         *            the assembly to link
         * @param wirelets
         *            optional wirelets
         * @return a container handle representing the linked container
         */
        ContainerHandle link(Assembly assembly, Wirelet... wirelets);

        ContainerHandle newContainer(Wirelet... wirelets);

        ContainerInstaller newLifetime();

        // Only Managed-Operation does not require a wrapper
        default ContainerInstaller wrapIn(InstanceBeanConfiguration<?> wrapperBeanConfiguration) {
            // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum supportere det
            // Hvis vi vil dele den...

            // Det betyder ogsaa vi skal lave en wrapper bean alene
            return null;
        }
    }
}

//BeanHandle<?> unwrap(BeanConfiguration configuration) {
//  // Can only call this on bean configurations that have been created by the extension itself.
//  // But then could people just store it in a map...
//  throw new UnsupportedOperationException();
//}

//// Ideen er at man fx kan have en handle.onInitialize(MyEBC, BeanHandle<Driver>, (b,p)->b.drivers[i]=p);

//// Ryger paa BeanHandle istedet for
//<B, P> void callbackOnInitialize(InstanceBeanConfiguration<B> extensionBean, BeanHandle<P> beanToInitialize, BiConsumer<? super B, ? super P> consumer) {
//    // ideen er at efter P er initialiseret saa kalder vi consumeren
//
//    // Smid den paa BeanHandle???
//    // <B> onInitialize(InstanceBeanConfiguration<B> extensionBean, BiConsumer<? super B, ? super P> consumer)
//    // <B> onInitialize(Class<B> extensionBeanClass, BiConsumer<? super B, ? super P> consumer)
//}
//
//// Same container I think0-=
//// Could we have it on initialize? Nahh, fungere vel egentligt kun med container beans
//<B, P> void callbackOnInitialize(InstanceBeanConfiguration<B> extensionBean, InstanceBeanConfiguration<P> beanToInitialize,
//        BiConsumer<? super B, ? super P> consumer) {
//    // Skal vi checke at consumerBean bliver initialiseret foerend provider bean???
//    // Ja det syntes jeg...
//    // Skal de vaere samme container??
//
//    // Packed will call consumer(T, P) once provideBean has been initialized
//    // Skal vi checke provideBean depends on consumerBean
//    // framework will call
//    // consumer(T, P) at initialization time
//
//}
// Idea was to return the same IBC always. But equals, hashcode is fixed on BeanConfiguration, so can use as key in maps
//class Tmp {
//  InstanceBeanConfiguration<T> conf;
//}
//Tmp tmp = new Tmp();
//BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER, useSite()).InstallIfAbsent(clazz, h -> action.accept(tmp.conf = new InstanceBeanConfiguration<>(h)));
//return tmp.conf == null ? new InstanceBeanConfiguration<>(handle) : tmp.conf;

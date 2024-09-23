package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanBuildHook;
import app.packed.bean.BeanBuildLocal.Accessor;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanElement;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.BeanTemplate;
import app.packed.binding.BindableVariable;
import app.packed.binding.ComputedConstant;
import app.packed.binding.Key;
import app.packed.binding.Provider;
import app.packed.build.BuildActor;
import app.packed.build.hook.BuildHook;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.context.Context;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.scanning.BeanScanner;
import internal.app.packed.bean.scanning.PackedBeanElement;
import internal.app.packed.binding.BindingAccessor;
import internal.app.packed.binding.BindingAccessor.FromCodeGenerated;
import internal.app.packed.binding.BindingAccessor.FromConstant;
import internal.app.packed.binding.BindingAccessor.FromLifetimeArena;
import internal.app.packed.binding.BindingAccessor.FromOperationResult;
import internal.app.packed.binding.SuppliedBindingKind;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.handlers.BeanHandlers;
import internal.app.packed.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.BeanOperationStore;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.PackedOp.NewOS;
import internal.app.packed.operation.PackedOperationInstaller;
import internal.app.packed.operation.PackedOperationInstaller.BeanFactoryOperationHandle;
import internal.app.packed.operation.PackedOperationTarget.BeanAccessOperationTarget;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.service.MainServiceNamespaceHandle;
import internal.app.packed.service.ServiceProviderSetup.BeanServiceProviderSetup;
import internal.app.packed.service.util.SequencedServiceMap;

/** The internal configuration of a bean. */
public final class BeanSetup implements ContextualizedElementSetup, BuildLocalSource, ComponentSetup {

    /** A list ofIllegal bean classes. Void is technically allowed but {@link #installWithoutSource()} needs to used. */
    // TODO Align with Key and allowed classes
    public static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Class.class, Key.class, Op.class, Optional.class, Provider.class);

    /** The bean class, is typical void.class for functional beans. */
    public final Class<?> beanClass;

    /** The kind of bean. */
    public final BeanKind beanKind;

    /** Bean services that have been bound specifically to the bean. */
    public final SequencedServiceMap<BindableVariable> beanServices = new SequencedServiceMap<>();

    public final SequencedServiceMap<BeanServiceProviderSetup> serviceProviders = new SequencedServiceMap<>();

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, otherwise the bean instance) */
    @Nullable
    public final Object beanSource;

    /** The type of source the installer is created from. */
    public final BeanSourceKind beanSourceKind;

    /** The container this bean is installed in. */
    public final ContainerSetup container;

    private HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /**
     * The bean's handle. Initialized from {@link #newBean(PackedBeanInstaller, Class, BeanSourceKind, Object, Function)}
     */
    @Nullable
    private BeanHandle<?> handle;

    /** The extension that installed the bean. */
    public final ExtensionSetup installedBy;

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** An index into a container lifetime store, or -1. */
    public final int lifetimeStoreIndex;

    public int multiInstall;

    /** The name of this bean. Should only be updated by {@link internal.app.packed.container.ContainerBeanStore}. */
    String name;

    /** The operations of this bean. */
    public final BeanOperationStore operations = new BeanOperationStore();

    /** The owner of the bean. */
    public final AuthoritySetup<?> owner;

    @Nullable
    public BeanScanner scanner;

    /** Create a new bean. */
    private BeanSetup(PackedBeanInstaller installer, Class<?> beanClass, BeanSourceKind beanSourceKind, @Nullable Object beanSource) {
        this.beanKind = requireNonNull(installer.template.kind());
        this.beanClass = requireNonNull(beanClass);
        this.beanSource = beanSource;
        this.beanSourceKind = requireNonNull(beanSourceKind);

        this.container = requireNonNull(installer.container);
        this.installedBy = requireNonNull(installer.installingExtension);
        this.owner = requireNonNull(installer.owner);

        // Set the lifetime of the bean
        ContainerLifetimeSetup containerLifetime = container.lifetime;
        if (beanKind == BeanKind.CONTAINER || beanKind == BeanKind.STATIC) {
            this.lifetime = containerLifetime;
            this.lifetimeStoreIndex = container.lifetime.addBean(this);
        } else {
            BeanLifetimeSetup bls = new BeanLifetimeSetup(this, installer);
            this.lifetime = bls;
            this.lifetimeStoreIndex = -1;
            // containerLifetime.addDetachedChildBean(bls);
        }
        for (PackedContextTemplate t : installer.template.contexts().values()) {
            contexts.put(t.contextClass(), new ContextSetup(t, this));
        }
    }

    public BindingAccessor beanInstanceBindingProvider() {
        if (beanSourceKind == BeanSourceKind.INSTANCE) {
            return new FromConstant(beanSource.getClass(), beanSource);
        } else if (beanKind == BeanKind.CONTAINER) { // we've already checked if instance
            return new FromLifetimeArena(container.lifetime, lifetimeStoreIndex, beanClass);
        } else if (beanKind == BeanKind.UNMANAGED) {
            return new FromOperationResult(operations.first());
        }
        throw new Error();
    }

    public <K> void bindCodeGenerator(Key<K> key, Supplier<? extends K> supplier) {
        requireNonNull(key, "key is null");
        requireNonNull(supplier, "supplier is null");

        // Add the service provider for the bean
        serviceProviders.put(key, new BeanServiceProviderSetup(key, new FromCodeGenerated(supplier, SuppliedBindingKind.CODEGEN)));

        SequencedServiceMap<BindableVariable> m = beanServices;
        BindableVariable var = m.get(key);
        if (var == null) {
            throw new IllegalArgumentException("The specified bean must have an injection site that uses @" + ComputedConstant.class.getSimpleName() + " " + key
                    + ". Available " + m.keySet());
        } else if (var.isBound()) {
            throw new IllegalStateException("A supplier has previously been provided for key [key = " + key + ", bean = " + this + "]");
        }

        var.bindComputedConstant(supplier);
    }

    /** {@inheritDoc} */
    public ComponentPath componentPath() {
        return ComponentKind.BEAN.pathNew(container.componentPath(), name());
    }

    public Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        for (OperationSetup os : operations) {
            result.addAll(os.dependsOn());
        }
        return result;
    }

    @Override
    @Nullable
    public ContextSetup findContext(Class<? extends Context<?>> contextClass) {
        Class<? extends Context<?>> cl = ContextInfo.normalize(contextClass);
        ContextSetup cs = contexts.get(cl);
        if (cs != null) {
            return cs;
        }
        return container.findContext(cl);
    }

    /** {@inheritDoc} */
    @Override
    public void forEachContext(Consumer<? super ContextSetup> action) {
        contexts.values().forEach(action);
        container.forEachContext(action);
    }

    /**
     * {@return the handle of the bean}
     */
    public BeanHandle<?> handle() {
        return requireNonNull(handle);
    }

    public Iterable<BuildHook> hooks() {
        return Collections.emptyList();
    }

    // Relative to x
    public OperationSetup instanceAccessOperation() {
        PackedOperationTemplate template = (PackedOperationTemplate) OperationTemplate.defaults().reconfigure(c -> c.returnType(beanClass));

        PackedOperationInstaller installer = template.newInstaller(OperationType.of(beanClass), this, installedBy);
        installer.operationTarget = new BeanAccessOperationTarget();
        installer.namePrefix = "InstantAccess";

        return OperationSetup.crack(installer.install(OperationHandle::new));
    }

    /** {@return a map of locals for the bean} */
    @Override
    public BuildLocalMap locals() {
        return container.locals();
    }

    /** {@return a new mirror.} */
    @Override
    public BeanMirror mirror() {
        return handle().mirror();
    }

    public String name() {
        return name;
    }

    // I think we name beans BaseExtension#main
    // I think we name beans SchedulingExtension#main
    public void named(String newName) {
        container.beans.updateBeanName(this, newName);
    }
//
//    /** {@return the path of this component} */
//    public OldApplicationPath path() {
//        int size = container.node.depth();
//        String[] paths = new String[size + 1];
//        paths[size] = name();
//        ContainerSetup c = container;
//        // check for null instead...
//        for (int i = size - 1; i >= 0; i--) {
//            paths[i] = c.node.name;
//            c = c.node.parent;
//        }
//        return new PackedNamespacePath(paths);
//    }

    public BuildActor owner() {
        return owner.authority();
    }

    public MainServiceNamespaceHandle serviceNamespace() {
        if (owner instanceof ExtensionSetup es) {
            return es.sm();
        } else {
            return container.servicesMain();
        }
    }

    @Override
    public String toString() {
        return "Bean Name " + name;
    }

    /**
     * Extracts the actual bean setup from the specified accessor.
     *
     * @param accessor
     *            the accessor to extract from
     * @return the extracted bean
     */
    public static BeanSetup crack(Accessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return switch (accessor) {
        case BeanConfiguration b -> crack(b);
        case BeanElement b -> crack(b);
        case BeanHandle<?> b -> crack(b);
        case BeanIntrospector b -> crack(b);
        case BeanMirror b -> crack(b);
        };
    }

    public boolean isConfigurable() {

        return true;
    }

    /**
     * Extracts a bean setup from a bean configuration.
     *
     * @param configuration
     *            the configuration to extract from
     * @return the bean setup
     */
    public static BeanSetup crack(BeanConfiguration configuration) {
        return crack(BeanHandlers.getBeanConfigurationHandle(configuration));
    }

    public static BeanSetup crack(BeanElement element) {
        return ((PackedBeanElement) element).bean();
    }

    public static BeanSetup crack(BeanHandle<?> handle) {
        return BeanHandlers.getBeanHandleBean(handle);
    }

    public static BeanSetup crack(BeanIntrospector introspector) {
        return BeanHandlers.invokeBeanIntrospectorBean(introspector);
    }

    public static BeanSetup crack(BeanMirror mirror) {
        return crack(BeanHandlers.getBeanMirrorHandle(mirror));
    }

    /**
     * Creates the new bean using this installer as the configuration.
     *
     * @param <C>
     *            the type of bean configuration that is returned to the user
     * @param beanClass
     *            the bean class
     * @param sourceKind
     *            the source of the bean
     * @param source
     *            the source of the bean
     * @param newConfiguration
     *            a function responsible for creating the bean's configuration
     * @return a handle for the bean
     */
    @SuppressWarnings("unchecked")
    static <H extends BeanHandle<?>> H newBean(PackedBeanInstaller installer, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source,
            Function<? super BeanTemplate.Installer, H> factory) {
        requireNonNull(factory, "factory is null");
        if (sourceKind != BeanSourceKind.SOURCELESS && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }
        installer.checkNotInstalledYet(); // TODO check extension/assembly can still install beans

        // Create the Bean, this also marks this installer as unconfigurable
        BeanSetup bean = installer.install(new BeanSetup(installer, beanClass, sourceKind, source));

        // Creating an bean factory operation representing the Op if an Op was specified when creating the bean.
        if (sourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.beanSource;
            PackedOperationTemplate ot;
            if (bean.lifetime.lifetimes().isEmpty()) {
                ot = (PackedOperationTemplate) OperationTemplate.defaults();
            } else {
                ot = bean.lifetime.lifetimes().get(0).template;
            }

            // TODO should be marked as bean factory
            op.newOperationSetup(new NewOS(bean, bean.installedBy, ot, BeanFactoryOperationHandle::new, null));

            // Op'en bliver resolved med BeanClassen i scanneren...
            // Ved ikke om det giver mening, vil umiddelbart sige nej
            // Vil sige den er helt uafhaendig? Men for nu er det fint
        }

        // Scan the bean class for annotations if it has a source

        // We need this here to access mirrors when binding them as constants
        // Maybe we should bind them delayed.
        BeanHandle<?> apply = factory.apply(installer);
        bean.handle = apply;
        if (sourceKind != BeanSourceKind.SOURCELESS) {
            new BeanScanner(bean).introspect();
            bean.scanner = null;
        }

        // Add the bean to the container and initialize the name of the bean
        bean.container.beans.installAndSetBeanName(bean, installer.namePrefix);

        bean.container.assembly.model.hooks.forEach(BeanBuildHook.class, h -> h.onNew(apply.configuration()));

        return (H) apply;
    }

}

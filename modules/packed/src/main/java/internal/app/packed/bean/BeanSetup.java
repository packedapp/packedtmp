package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.application.ApplicationPath;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.LifecycleOperationMirror;
import app.packed.container.Realm;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.bean.BeanHandle;
import app.packed.extension.operation.OperationHandle;
import app.packed.extension.operation.OperationTemplate;
import app.packed.util.FunctionType;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingResolution.FromConstant;
import internal.app.packed.binding.BindingResolution.FromLifetimeArena;
import internal.app.packed.binding.BindingResolution.FromOperation;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.NameCheck;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.BeanAccessOperationSetup;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.PackedNamespacePath;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/** The internal configuration of a bean. */
public final class BeanSetup {

    /** A MethodHandle for invoking {@link BeanMirror#initialize(BeanSetup)}. */
    private static final MethodHandle MH_BEAN_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), BeanMirror.class, "initialize", void.class,
            BeanSetup.class);

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BEAN_CONFIGURATION_TO_HANDLE = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanConfiguration.class, "handle",
            BeanHandle.class);

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BEAN_INTROSPECTOR_TO_THIS = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanIntrospector.class, "setup",
            BeanScannerExtension.class);

    /** The bean class, is typical void.class for functional beans. */
    public final Class<?> beanClass;

    /** The kind of bean. */
    public final BeanKind beanKind;

    /** All beans in a container are maintained in a linked list. */
    @Nullable
    public BeanSetup beanSiblingNext;

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, otherwise an instance) */
    @Nullable
    public final Object beanSource;

    /** The type of source the installer is created from. */
    public final BeanSourceKind beanSourceKind;

    /** The container this bean is installed in. */
    public final ContainerSetup container;

    public ContextSetup contexts;

    /** The extension that installed the bean. */
    public final ExtensionSetup installedBy;

    public final ArrayList<BeanLifecycleOperation> lifecycleOperations = new ArrayList<>();

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** An index into a container lifetime store, or -1. */
    public final int lifetimeStoreIndex;

    /** A bean local map. */
    public final IdentityHashMap<PackedBeanLocal<?>, Object> locals;

    /** Supplies a mirror for the operation */
    @Nullable
    private Supplier<? extends BeanMirror> mirrorSupplier;

    /** The name of this bean. Should only be updated through {@link #named(String)} */
    String name;

    /** Operations declared by the bean. */
    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /** The owner of the bean. */
    public final BeanOwner owner;

    // ???
    public boolean providingOperationsVisited;

    /** A list of services provided by the bean, used for circular dependency checks. */
    public final List<ServiceProviderSetup> serviceProviders = new ArrayList<>();

    /** Create a new bean. */
    BeanSetup(PackedBeanInstaller installer, Class<?> beanClass, BeanSourceKind beanSourceKind, @Nullable Object beanSource) {
        this.beanKind = requireNonNull(installer.template.kind);
        this.beanClass = requireNonNull(beanClass);
        this.beanSource = beanSource;
        this.beanSourceKind = requireNonNull(beanSourceKind);

        this.container = requireNonNull(installer.container);
        this.installedBy = requireNonNull(installer.installingExtension);
        this.owner = requireNonNull(installer.owner);

        this.mirrorSupplier = installer.supplier;

        this.locals = installer.locals;

        // Set the lifetime of the bean
        ContainerLifetimeSetup containerLifetime = container.lifetime;
        if (beanKind == BeanKind.CONTAINER || beanKind == BeanKind.STATIC) {
            this.lifetime = containerLifetime;
            this.lifetimeStoreIndex = container.lifetime.addBean(this);
        } else {
            BeanLifetimeSetup bls = new BeanLifetimeSetup(this, installer);
            this.lifetime = bls;
            this.lifetimeStoreIndex = -1;
            containerLifetime.addDetachedChildBean(bls);
        }
    }

    public void addLifecycleOperation(BeanLifecycleOrder runOrder, OperationHandle operation) {
        lifecycleOperations.add(new BeanLifecycleOperation(runOrder, operation));
        operation.specializeMirror(() -> new LifecycleOperationMirror());
    }

    public BindingResolution beanInstanceBindingProvider() {
        if (beanSourceKind == BeanSourceKind.INSTANCE) {
            return new FromConstant(beanSource.getClass(), beanSource);
        } else if (beanKind == BeanKind.CONTAINER) { // we've already checked if instance
            return new FromLifetimeArena(container.lifetime, lifetimeStoreIndex, beanClass);
        } else if (beanKind == BeanKind.MANYTON) {
            return new FromOperation(operations.get(0));
        }
        throw new Error();
    }

    public Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        for (OperationSetup os : operations) {
            result.addAll(os.dependsOn());
        }
        return result;
    }

    // Relative to x
    public OperationSetup instanceAccessOperation() {
        OperationTemplate template = OperationTemplate.defaults().withReturnType(beanClass);
        return new BeanAccessOperationSetup(installedBy, this, FunctionType.of(beanClass), template);
    }

    /** {@return a new mirror.} */
    public BeanMirror mirror() {
        BeanMirror mirror = ClassUtil.mirrorHelper(BeanMirror.class, BeanMirror::new, mirrorSupplier);

        // Initialize BeanMirror by calling BeanMirror#initialize(BeanSetup)
        try {
            MH_BEAN_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public String name() {
        return name;
    }

    public void named(String newName) {
        // We start by validating the new name of the component
        NameCheck.checkComponentName(newName);

        // Check that this component is still active and the name can be set
        // Do we actually care? Of course we can only set as long as the realm is open
        // But other than that why not
        // Issue should be the container which should probably work identical
        // And I do think we should have it as the first thing

        if (container.children.putIfAbsent(newName, this) != null) {
            if (newName.equals(name)) { // tried to set the current name which is okay i guess?
                return;
            }
            throw new IllegalArgumentException("A bean or container with the specified name '" + newName + "' already exists");
        }
        container.children.remove(name);
        this.name = newName;
    }

    public Realm owner() {
        return owner.realm();
    }

    /** {@return the path of this component} */
    public ApplicationPath path() {
        int size = container.depth();
        String[] paths = new String[size + 1];
        paths[size] = name;
        ContainerSetup c = container;
        // check for null instead...
        for (int i = size - 1; i >= 0; i--) {
            paths[i] = c.name;
            c = c.treeParent;
        }
        return new PackedNamespacePath(paths);
    }

    /**
     * Extracts a bean setup from a bean configuration.
     *
     * @param configuration
     *            the configuration to extract from
     * @return the bean setup
     */
    public static BeanSetup crack(BeanConfiguration configuration) {
        requireNonNull(configuration, "configuration is null");
        PackedBeanHandle<?> handle = (PackedBeanHandle<?>) VH_BEAN_CONFIGURATION_TO_HANDLE.get(configuration);
        return handle.bean();
    }

    public static BeanSetup crack(BeanHandle<?> handle) {
        return ((PackedBeanHandle<?>) handle).bean();
    }

    public static BeanSetup crack(BeanIntrospector introspector) {
        requireNonNull(introspector, "introspector is null");
        return ((BeanScannerExtension) VH_BEAN_INTROSPECTOR_TO_THIS.get(introspector)).scanner.bean;
    }

    /**
     * Extracts a bean setup from a bean handle.
     *
     * @param handle
     *            the handle to extract from
     * @return the bean setup
     */
    public static BeanSetup crack(BeanMethod m) {
        return ((PackedBeanMethod) m).extension.scanner.bean;
    }
}

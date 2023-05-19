package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.application.OldApplicationPath;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.container.Author;
import app.packed.context.Context;
import app.packed.extension.BeanElement;
import app.packed.extension.BeanIntrospector;
import app.packed.lifetime.LifecycleOperationMirror;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingResolution.FromConstant;
import internal.app.packed.binding.BindingResolution.FromLifetimeArena;
import internal.app.packed.binding.BindingResolution.FromOperation;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.NameCheck;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.BeanAccessOperationSetup;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.MagicInitializer;
import internal.app.packed.util.PackedNamespacePath;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;

/** The internal configuration of a bean. */
public final class BeanSetup implements ContextualizedElementSetup {

    /** A magic initializer for {@link BeanMirror}. */
    public static final MagicInitializer<BeanSetup> MIRROR_INITIALIZER = MagicInitializer.of(BeanMirror.class);

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BEAN_CONFIGURATION_TO_HANDLE = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanConfiguration.class, "handle",
            PackedBeanHandle.class);

    boolean allowMultiClass;

    /** The bean class, is typical void.class for functional beans. */
    public final Class<?> beanClass;

    /** The kind of bean. */
    public final BeanKind beanKind;

    /** All beans in a container are maintained in a linked list. */
    @Nullable
    public BeanSetup beanSiblingNext;

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, otherwise the bean instance) */
    @Nullable
    public final Object beanSource;

    /** The type of source the installer is created from. */
    public final BeanSourceKind beanSourceKind;

    /** The container this bean is installed in. */
    public final ContainerSetup container;

    private HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /** The extension that installed the bean. */
    public final ExtensionSetup installedBy;

    /**
     * All lifecycle operations for the bean. Is initially unsorted as operations can be added in any order. But in the end
     * the list will be sorted in the order of execution. With {@link app.packed.lifetime.RunState#INITIALIZING} lifecycle
     * operations first, and {@link app.packed.lifetime.RunState#STOPPING} lifecycle operations last.
     */
    public final ArrayList<BeanLifecycleOperation> lifecycleOperations = new ArrayList<>();

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** An index into a container lifetime store, or -1. */
    public final int lifetimeStoreIndex;

    /** Supplies a specialized mirror for the operation. */
    @Nullable
    private final Supplier<? extends BeanMirror> mirrorSupplier;

    /** The name of this bean. Should only be updated through {@link #named(String)} */
    String name;

    /**
     * The unique name of every operation.
     * <p>
     * We map a operation setup to a string instead of the other way around. So that
     * {@link app.packed.operation.OperationMirror#name()} is fast.
     * <p>
     * This is lazily generated primarily for use in mirrors. We generate it lazily because calculating the unique names of
     * operations is actually a bit time consuming.
     */
    private volatile Map<OperationSetup, String> operationNames;

    /** Operations declared by the bean. */
    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /** The owner of the bean. */
    public final AuthorSetup owner;

    public boolean providingOperationsVisited;

    /** A list of services provided by the bean, used for circular dependency checks. */
    public final List<ServiceProviderSetup> serviceProviders = new ArrayList<>();

    /** Create a new bean. */
    BeanSetup(PackedBeanBuilder installer, Class<?> beanClass, BeanSourceKind beanSourceKind, @Nullable Object beanSource) {
        this.beanKind = requireNonNull(installer.template.kind());
        this.beanClass = requireNonNull(beanClass);
        this.beanSource = beanSource;
        this.beanSourceKind = requireNonNull(beanSourceKind);

        this.container = requireNonNull(installer.container);
        this.installedBy = requireNonNull(installer.installingExtension);
        this.owner = requireNonNull(installer.owner);

        this.mirrorSupplier = installer.supplier;

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
    }

    public void addLifecycleOperation(BeanLifecycleOrder runOrder, OperationHandle operation) {
        lifecycleOperations.add(new BeanLifecycleOperation(runOrder, operation));
        operation.specializeMirror(() -> new LifecycleOperationMirror());
    }

    public Author author() {
        return owner.author();
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

    @Override
    public void forEachContext(BiConsumer<? super Class<? extends Context<?>>, ? super ContextSetup> action) {
        contexts.forEach(action);
        container.forEachContext(action);
    }

    // Relative to x
    public OperationSetup instanceAccessOperation() {
        OperationTemplate template = OperationTemplate.defaults().returnType(beanClass);
        return new BeanAccessOperationSetup(installedBy, this, OperationType.of(beanClass), template);
    }

    /** {@return a new mirror.} */
    @Override
    public BeanMirror mirror() {
        return MIRROR_INITIALIZER.run(() -> ClassUtil.newMirror(BeanMirror.class, BeanMirror::new, mirrorSupplier), this);
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

        if (container.namedChildren.putIfAbsent(newName, this) != null) {
            if (newName.equals(name)) { // tried to set the current name which is okay i guess?
                return;
            }
            throw new IllegalArgumentException("A bean or container with the specified name '" + newName + "' already exists");
        }
        container.namedChildren.remove(name);
        this.name = newName;
    }

    /**
     * <p>
     * We lazily calculate
     *
     * @return a map of operation to
     */
    public Map<OperationSetup, String> operationNames() {
        Map<OperationSetup, String> m = operationNames;
        // operationNames is only valid as
        if (m == null || m.size() != operationNames.size()) {
            m = operationNames = LazyNamer.calculate(operations, OperationSetup::namePrefix);
        }
        return m;
    }

    /** {@return the path of this component} */
    public OldApplicationPath path() {
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
        PackedBeanHandle<?> handle = (PackedBeanHandle<?>) VH_BEAN_CONFIGURATION_TO_HANDLE.get(configuration);
        return handle.bean();
    }

    public static BeanSetup crack(BeanElement element) {
        return ((PackedBeanElement) element).bean();
    }




    /** A MethodHandle for invoking {@link ExtensionMirror#initialize(ExtensionSetup)}. */
    private static final MethodHandle MH_BEAN_INTROSPECTOR_TO_BEAN = LookupUtil.findVirtual(MethodHandles.lookup(), BeanIntrospector.class, "bean",
            BeanSetup.class);

    public static BeanSetup crack(BeanIntrospector introspector) {
        // Call ExtensionMirror#initialize(ExtensionSetup)
        try {
            return (BeanSetup) MH_BEAN_INTROSPECTOR_TO_BEAN.invokeExact(introspector);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    public static BeanSetup crack(BeanHandle<?> handle) {
        return ((PackedBeanHandle<?>) handle).bean();
    }

}

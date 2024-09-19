package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal.Accessor;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.ComputedConstant;
import app.packed.build.hook.BuildHook;
import app.packed.component.Authority;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.context.Context;
import app.packed.extension.BeanElement;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.BindableVariable;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingResolution.FromConstant;
import internal.app.packed.binding.BindingResolution.FromLifetimeArena;
import internal.app.packed.binding.BindingResolution.FromOperationResult;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.AuthoritySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.BeanOperationStore;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationInstaller;
import internal.app.packed.operation.PackedOperationTarget.BeanAccessOperationSetup;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.service.ServiceNamespaceSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * The internal configuration of a bean.
 *
 * @implNote The reason this class does not directly implement BeanHandle is because the BeanHandle interface is
 *           parameterised.
 */
public final class BeanSetup implements ComponentSetup, ContextualizedElementSetup, BuildLocalSource {

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_BEAN_HANDLE_ON_ASSEMBLY_CLOSE = LookupUtil.findVirtual(MethodHandles.lookup(), BeanHandle.class, "onAssemblyClose",
            void.class);

    /** A bean local for variables that use {@link app.packed.extension.BaseExtensionPoint.CodeGenerated}. */
    public static final PackedBeanLocal<Map<Key<?>, BindableVariable>> CODEGEN = new PackedBeanLocal<>(() -> new HashMap<>());

    /** Call {@link Extension#onAssemblyClose()}. */
    public void invokeBeanOnAssemblyClose() {
        try {
            MH_BEAN_HANDLE_ON_ASSEMBLY_CLOSE.invokeExact(handle);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    /** A MethodHandle for invoking {@link BeanIntrospector#bean}. */
    private static final MethodHandle MH_BEAN_INTROSPECTOR_TO_SETUP = LookupUtil.findVirtual(MethodHandles.lookup(), BeanIntrospector.class, "bean",
            BeanSetup.class);

    /** A handle that can access {@link BeanConfiguration#handle}. */
    private static final VarHandle VH_BEAN_CONFIGURATION_TO_HANDLE = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanConfiguration.class, "handle",
            BeanHandle.class);

    /** A handle that can access {@link BeanHandleHandle#bean}. */
    private static final VarHandle VH_BEAN_HANDLE_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanHandle.class, "bean", BeanSetup.class);

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BEAN_MIRROR_TO_HANDLE = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanMirror.class, "handle", BeanHandle.class);

    // TODO Specialized bindings we will look up before a service
    public final HashMap<Key<?>, ?> beanBindings = new HashMap<>();

    /** The bean class, is typical void.class for functional beans. */
    public final Class<?> beanClass;

    /** The kind of bean. */
    public final BeanKind beanKind;

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, otherwise the bean instance) */
    @Nullable
    public final Object beanSource;

    /** The type of source the installer is created from. */
    public final BeanSourceKind beanSourceKind;

    /** The container this bean is installed in. */
    public final ContainerSetup container;

    private HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    @Nullable
    BeanHandle<?> handle;

    /** The extension that installed the bean. */
    public final ExtensionSetup installedBy;

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** An index into a container lifetime store, or -1. */
    public final int lifetimeStoreIndex;

    public int multiInstall;

    /** The name of this bean. Should only be updated by {@link internal.app.packed.container.ContainerBeanStore}. */
    public String name;

    /** The operations of this bean. */
    public final BeanOperationStore operations = new BeanOperationStore();

    /** The owner of the bean. */
    public final AuthoritySetup owner;

    @Nullable
    public BeanScanner scanner;

    /** Create a new bean. */
    BeanSetup(PackedBeanInstaller installer, Class<?> beanClass, BeanSourceKind beanSourceKind, @Nullable Object beanSource) {
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
    }

    public <K> void addCodeGenerated(Key<K> key, Supplier<? extends K> supplier) {
        requireNonNull(key, "key is null");
        requireNonNull(supplier, "supplier is null");

//      if (true) {
//          addCodeGenerated(key, supplier);
//      }
        Map<Key<?>, BindableVariable> m = locals().get(BeanSetup.CODEGEN, this);
        BindableVariable var = m.get(key);
        if (var == null) {
            throw new IllegalArgumentException("The specified bean must have an injection site that uses @" + ComputedConstant.class.getSimpleName() + " " + key
                    + ". Available " + m.keySet());
        } else if (var.isBound()) {
            throw new IllegalStateException("A supplier has previously been provided for key [key = " + key + ", bean = " + this + "]");
        }

        var.bindComputedConstant(supplier);
    }

    public BindingResolution beanInstanceBindingProvider() {
        if (beanSourceKind == BeanSourceKind.INSTANCE) {
            return new FromConstant(beanSource.getClass(), beanSource);
        } else if (beanKind == BeanKind.CONTAINER) { // we've already checked if instance
            return new FromLifetimeArena(container.lifetime, lifetimeStoreIndex, beanClass);
        } else if (beanKind == BeanKind.UNMANAGED) {
            return new FromOperationResult(operations.first());
        }
        throw new Error();
    }

    /** {@inheritDoc} */
    @Override
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
    public void forEachContext(BiConsumer<? super Class<? extends Context<?>>, ? super ContextSetup> action) {
        contexts.forEach(action);
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
        installer.pot = new BeanAccessOperationSetup();
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

    public Authority owner() {
        return owner.authority();
    }

    public ServiceNamespaceSetup serviceNamespace() {
        if (owner instanceof ExtensionSetup es) {
            return es.sm;
        } else {
            return container.sm;
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

    /**
     * Extracts a bean setup from a bean configuration.
     *
     * @param configuration
     *            the configuration to extract from
     * @return the bean setup
     */
    public static BeanSetup crack(BeanConfiguration configuration) {
        BeanHandle<?> handle = (BeanHandle<?>) VH_BEAN_CONFIGURATION_TO_HANDLE.get(configuration);
        return crack(handle);
    }

    public static BeanSetup crack(BeanElement element) {
        return ((PackedBeanElement) element).bean();
    }

    public static BeanSetup crack(BeanHandle<?> handle) {
        return (BeanSetup) VH_BEAN_HANDLE_TO_SETUP.get(handle);
    }

    public static BeanSetup crack(BeanIntrospector introspector) {
        try {
            return (BeanSetup) MH_BEAN_INTROSPECTOR_TO_SETUP.invokeExact(introspector);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    public static BeanSetup crack(BeanMirror mirror) {
        BeanHandle<?> handle = (BeanHandle<?>) VH_BEAN_MIRROR_TO_HANDLE.get(mirror);
        return crack(handle);
    }
}

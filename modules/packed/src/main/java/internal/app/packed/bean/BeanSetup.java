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

import app.packed.bean.BeanBuildHook;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocalAccessor;
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
import app.packed.operation.OperationType;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingResolution.FromConstant;
import internal.app.packed.binding.BindingResolution.FromLifetimeArena;
import internal.app.packed.binding.BindingResolution.FromOperationResult;
import internal.app.packed.build.PackedLocalMap;
import internal.app.packed.build.PackedLocalMap.KeyAndLocalMapSource;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.component.PackedComponentTwin;
import internal.app.packed.container.AuthoritySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.BeanAccessOperationSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.MagicInitializer;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.operation.OperationTemplate;

/**
 * The internal configuration of a bean.
 *
 * @implNote The reason this class does not directly implement BeanHandle is because the BeanHandle interface is
 *           parameterised.
 */
public final class BeanSetup extends ComponentSetup implements PackedComponentTwin , ContextualizedElementSetup , KeyAndLocalMapSource {

    /** A bean local for variables that use {@link app.packed.extension.BaseExtensionPoint.CodeGenerated}. */
    public static final PackedBeanLocal<Map<Key<?>, BindableVariable>> CODEGEN = new PackedBeanLocal<>(() -> new HashMap<>());

    /** A MethodHandle for invoking {@link BeanIntrospector#bean}. */
    private static final MethodHandle MH_BEAN_INTROSPECTOR_TO_BEAN = LookupUtil.findVirtual(MethodHandles.lookup(), BeanIntrospector.class, "bean",
            BeanSetup.class);

    /** A magic initializer for {@link BeanMirror}. */
    public static final MagicInitializer<BeanSetup> MIRROR_INITIALIZER = MagicInitializer.of(BeanMirror.class);

    /** A handle that can access {@link BeanConfiguration#handle}. */
    private static final VarHandle VH_BEAN_CONFIGURATION_TO_HANDLE = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanConfiguration.class, "handle",
            PackedBeanHandle.class);

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BEAN_MIRROR_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanMirror.class, "bean", BeanSetup.class);

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

    /** The configuration representing this bean, is set from {@link #initConfiguration(BeanConfiguration)}. */
    @Nullable
    private BeanConfiguration configuration;

    /** The container this bean is installed in. */
    public final ContainerSetup container;

    private HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /** The extension that installed the bean. */
    public final ExtensionSetup installedBy;

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** An index into a container lifetime store, or -1. */
    public final int lifetimeStoreIndex;

    /** Supplies a specialized mirror for the operation. */
    @Nullable
    private final Supplier<? extends BeanMirror> mirrorSupplier;

    public int multiInstall;

    /** The name of this bean. Should only be updated by {@link internal.app.packed.container.ContainerBeanStore}. */
    public String name;

    /** The operations of this bean. */
    public final BeanOperationStore operations = new BeanOperationStore();

    /** The owner of the bean. */
    public final AuthoritySetup owner;

    /** Create a new bean. */
    BeanSetup(PackedBeanInstaller installer, Class<?> beanClass, BeanSourceKind beanSourceKind, @Nullable Object beanSource) {
        this.beanKind = requireNonNull(installer.template.kind());
        this.beanClass = requireNonNull(beanClass);
        this.beanSource = beanSource;
        this.beanSourceKind = requireNonNull(beanSourceKind);

        this.container = requireNonNull(installer.container);
        this.installedBy = requireNonNull(installer.installingExtension);
        this.owner = requireNonNull(installer.owner);

        this.mirrorSupplier = installer.mirrorSupplier;

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
            return new FromOperationResult(operations.all.get(0));
        }
        throw new Error();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return ComponentKind.BEAN.pathNew(container.componentPath(), name());
    }

    public BeanConfiguration configuration() {
        BeanConfiguration bc = configuration;
        if (bc == null) {
            throw new IllegalStateException("Cannot call this method from the constructor of the bean configuration");
        }
        return bc;
    }

    public Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        for (OperationSetup os : operations.all) {
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

    public Iterable<BuildHook> hooks() {
        return Collections.emptyList();
    }

    /**
     * Initializes the bean configuration.
     *
     * @param configuration
     *
     * @throws IllegalStateException
     *             if attempting to create multiple bean configurations for a single bean
     */
    void initConfiguration(BeanConfiguration configuration) {
        if (this.configuration != null) {
            throw new IllegalStateException("A bean installer can only be used to create a single bean");
        }
        this.configuration = requireNonNull(configuration);
        this.container.assembly.model.hooks.forEach(BeanBuildHook.class, h -> h.onNew(configuration));
    }

    // Relative to x
    public OperationSetup instanceAccessOperation() {
        OperationTemplate template = OperationTemplate.defaults().returnType(beanClass);
        return new BeanAccessOperationSetup(installedBy, this, OperationType.of(beanClass), template);
    }

    /** {@return a map of locals for the bean} */
    @Override
    public PackedLocalMap locals() {
        return container.locals();
    }

    /** {@return a new mirror.} */
    @Override
    public BeanMirror mirror() {
        return MIRROR_INITIALIZER.run(() -> ClassUtil.newMirror(BeanMirror.class, BeanMirror::new, mirrorSupplier), this);
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

    @Override
    public String toString() {
        return "Bean Name " + name;
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

    public static BeanSetup crack(BeanHandle<?> handle) {
        return ((PackedBeanHandle<?>) handle).bean();
    }

    public static BeanSetup crack(BeanIntrospector introspector) {
        try {
            return (BeanSetup) MH_BEAN_INTROSPECTOR_TO_BEAN.invokeExact(introspector);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    /**
     * Extracts the actual bean setup from the specified accessor.
     *
     * @param accessor
     *            the accessor to extract from
     * @return the extracted bean
     */
    public static BeanSetup crack(BeanLocalAccessor accessor) {
        return switch (accessor) {
        case BeanConfiguration b -> crack(b);
        case BeanElement b -> crack(b);
        case BeanHandle<?> b -> crack(b);
        case BeanIntrospector b -> crack(b);
        case BeanMirror b -> crack(b);
        };
    }

    public static BeanSetup crack(BeanMirror mirror) {
        return (BeanSetup) VH_BEAN_MIRROR_TO_SETUP.get(mirror);
    }
}

package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanExtension;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHandle.Option;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.MultipleBeanOfSameTypeDefinedException;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.UserOrExtension;
import app.packed.operation.Op;
import app.packed.operation.Provider;
import internal.app.packed.bean.PackedBeanHandle.InstallerOption;
import internal.app.packed.container.BeanOrContainerSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.BeanOperationSetup;
import internal.app.packed.operation.op.PackedOp;
import internal.app.packed.service.inject.BeanInjectionManager;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.PackedNamespacePath;
import internal.app.packed.util.ThrowableUtil;

/** The build-time configuration of a bean. */
public final class BeanSetup extends BeanOrContainerSetup implements BeanInfo {

    /** A MethodHandle for invoking {@link BeanMirror#initialize(BeanSetup)}. */
    private static final MethodHandle MH_BEAN_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BeanMirror.class, "initialize",
            void.class, BeanSetup.class);

    /** A handle that can access BeanConfiguration#beanHandle. */
    private static final VarHandle VH_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanConfiguration.class, "handle",
            PackedBeanHandle.class);

    /** The container this bean is registered in. */
    public final ContainerSetup container;

    /** The bean's injection manager. Null for functional beans, otherwise non-null */
    @Nullable
    public final BeanInjectionManager injectionManager;

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** Supplies a mirror for the operation */
    Supplier<? extends BeanMirror> mirrorSupplier = () -> new BeanMirror();

    @Nullable
    public Runnable onWiringAction;

    /** Operations declared by the bean. */
    public final ArrayList<BeanOperationSetup> operations = new ArrayList<>();

    /** Various properties of the bean. */
    public final BeanProps props;

    /**
     * Create a new bean setup.
     * 
     * @param props
     *            the handle builder
     */
    public BeanSetup(RealmSetup owner, BeanProps props) {
        super(owner);
        this.props = props;
        this.container = props.operator().container;
        this.lifetime = container.lifetime();

        if (props.kind() == BeanKind.FUNCTIONAL) { // Not sure exactly when we need it
            this.injectionManager = null;
        } else {
            this.injectionManager = new BeanInjectionManager(this, props);
        }

        String initialName = "Functional";
        if (props.beanModel() != null) {
            initialName = props.beanModel().simpleName();
        }

        class MuInst {
            int counter;
        }
        Class<?> cl = props.beanClass();
        if (props.beanClass() != void.class) {
            if (props.multiInstall()) {
                MuInst i = (MuInst) container.beanClassMap.compute(cl, (c, o) -> {
                    if (o == null) {
                        return new MuInst();
                    } else if (o instanceof BeanSetup) {
                        throw new MultipleBeanOfSameTypeDefinedException();
                    } else {
                        return o;
                    }
                });
                if (i.counter > 0) {
                    initialName = initialName + i.counter;
                }
                i.counter++;
            } else {
                container.beanClassMap.compute(cl, (c, o) -> {
                    if (o == null) {
                        return BeanSetup.this;
                    } else if (o instanceof BeanSetup) {
                        throw new MultipleBeanOfSameTypeDefinedException("A non-multi bean has already been defined for " + beanClass());
                    } else {
                        // We already have some multiple beans installed
                        throw new MultipleBeanOfSameTypeDefinedException();
                    }
                });
            }
        }

        // This will add it to the list of beans in the container
         container.initBeanName(this, initialName);
    }

    private static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Key.class, Op.class, Optional.class, Provider.class);

    static <T> BeanHandle<T> install(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, Class<?> beanClass, BeanKind kind,
            BeanSourceKind sourceKind, @Nullable Object source, BeanHandle.Option... options) {
        if (ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot register a bean with bean class " + beanClass);
        }

        // The various options, with default values.
        boolean nonUnique = false;
        BeanIntrospector customIntrospector = null;
        String namePrefix = null;

        // Process each option
        requireNonNull(options, "options is null");
        for (Option o : options) {
            requireNonNull(o, "option was null");
            if (o instanceof InstallerOption.CustomIntrospector ci) {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("Custom Introspector cannot be used with functional or static beans");
                }
                customIntrospector = ci.introspector();
            } else if (o instanceof InstallerOption.CustomPrefix cp) {
                namePrefix = cp.prefix();
            } else {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("NonUnique cannot be used with functional or static beans");
                }
                nonUnique = true;
            }
        }

        realm.wireCurrentComponent();

        BeanClassModel beanModel = sourceKind == BeanSourceKind.NONE ? null : new BeanClassModel(beanClass);
        BeanProps bp = new BeanProps(kind, beanClass, sourceKind, source, beanModel, operator, realm, extensionOwner, namePrefix, nonUnique);

        BeanSetup bean = new BeanSetup(realm, bp);

        // Scan the bean class for annotations unless the bean class is void or is from a java package
        if (sourceKind != BeanSourceKind.NONE && bean.beanClass().getModule() != Introspector.JAVA_BASE_MODULE) {
            new Introspector(bean, customIntrospector).introspect();
        }

        return new PackedBeanHandle<>(bean);
    }

    public BeanOperationSetup addOperation(BeanOperationSetup operation) {
        operations.add(requireNonNull(operation));
        return operation;
    }
    
    /** {@inheritDoc} */
    @Override
    public Class<?> beanClass() {
        return props.beanClass();
    }

    public LifetimeSetup lifetime() {
        return lifetime;
    }

    /** {@return a new mirror.} */
    public BeanMirror mirror() {
        // Create a new BeanMirror
        BeanMirror mirror = mirrorSupplier.get();
        if (mirror == null) {
            throw new NullPointerException(mirrorSupplier + " returned a null instead of an " + BeanMirror.class.getSimpleName() + " instance");
        }

        // Initialize BeanMirror by calling BeanMirror#initialize(BeanSetup)
        try {
            MH_BEAN_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public final void onWired() {
        Runnable w = onWiringAction;
        if (w != null) {
            w.run();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> operator() {
        return props.operator() == null ? BeanExtension.class : props.operator().extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public UserOrExtension owner() {
        return realm.realm();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup parent() {
        return container;
    }

    public static <T> BeanHandle<T> installClass(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind kind,
            Class<T> clazz, BeanHandle.Option... options) {
        requireNonNull(clazz, "clazz is null");
        return install(operator, realm, extensionOwner, clazz, kind, BeanSourceKind.CLASS, clazz, options);
    }

    public static BeanHandle<?> installFunctional(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner,
            BeanHandle.Option... options) {
        return install(operator, realm, extensionOwner, void.class, BeanKind.FUNCTIONAL, BeanSourceKind.NONE, null, options);
    }

    public static <T> BeanHandle<T> installInstance(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, T instance,
            BeanHandle.Option... options) {
        requireNonNull(instance, "instance is null");
        return install(operator, realm, extensionOwner, instance.getClass(), BeanKind.CONTAINER, BeanSourceKind.INSTANCE, instance, options);
    }

    public static <T> BeanHandle<T> installOp(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind kind, Op<T> op,
            BeanHandle.Option... options) {
        PackedOp<T> pop = PackedOp.crack(op);
        return install(operator, realm, extensionOwner, pop.type().returnType(), kind, BeanSourceKind.OP, pop, options);
    }

    /** {@return the path of this component} */
    public final NamespacePath path() {
        int depth = container.depth + 1;
        return switch (depth) {
        case 1 -> new PackedNamespacePath(name);
        default -> {
            String[] paths = new String[depth];
            paths[depth] = name;
            ContainerSetup acc = container;
            for (int i = depth - 1; i >= 0; i--) {
                paths[i] = acc.name;
                acc = acc.parent;
            }
            yield new PackedNamespacePath(paths);
        }
        };
    }

    public static BeanSetup crack(ExtensionBeanConfiguration<?> configuration) {
        PackedBeanHandle<?> bh = (PackedBeanHandle<?>) VH_HANDLE.get((BeanConfiguration) configuration);
        return bh.bean();
    }
}

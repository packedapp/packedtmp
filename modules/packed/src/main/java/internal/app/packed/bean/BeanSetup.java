package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.function.Supplier;

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanExtension;
import app.packed.bean.BeanMirror;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.UserOrExtension;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationType;
import internal.app.packed.container.BeanOrContainerSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.BeanOperationSetup;
import internal.app.packed.operation.OperationTarget;
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
    // Or maybe @Nullable BeanSetup; null -> container.eager
    public final LifetimeSetup lifetime;

    /** Supplies a mirror for the operation */
    Supplier<? extends BeanMirror> mirrorSupplier = () -> new BeanMirror();

    @Nullable
    public Runnable onWiringAction;

    /** Operations declared by the bean. */
    public final ArrayList<BeanOperationSetup> operations = new ArrayList<>();

    /** The Bean props. */
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

        if (props.beanClass() != void.class) { // Not sure exactly when we need it
            this.injectionManager = new BeanInjectionManager(this, props);
        } else {
            this.injectionManager = null;
        }

        String initialName = "Functional";
        if (props.beanModel() != null) {
            initialName = props.beanModel().simpleName();
        }

        container.initBeanName(this, initialName);
    }

    public void addOperation(BeanOperationSetup operation) {
        operations.add(requireNonNull(operation));
    }

    public BeanOperationSetup addOperation(ExtensionSetup extension, OperationType type, InvocationType invocationType, OperationTarget target) {
        BeanOperationSetup os = new BeanOperationSetup(this, type, extension, invocationType, target);
        operations.add(os);
        return os;
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

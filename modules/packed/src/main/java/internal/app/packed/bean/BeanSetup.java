package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.application.NamespacePath;
import app.packed.bean.BeanClassAlreadyExistsException;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.framework.Nullable;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanClassMapContainer.MuInst;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.container.NameCheck;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeOperation;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.LifetimePoolOperationSetup;
import internal.app.packed.operation.PackedInvocationType;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.service.ProvidedService;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.PackedNamespacePath;
import internal.app.packed.util.ThrowableUtil;

/** The internal configuration of a bean. */
public final class BeanSetup {

    /** A MethodHandle for invoking {@link BeanMirror#initialize(BeanSetup)}. */
    private static final MethodHandle MH_BEAN_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BeanMirror.class, "initialize",
            void.class, BeanSetup.class);

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BEAN_CONFIGURATION_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanConfiguration.class, "handle",
            BeanHandle.class);

    /** A handle that can access BeanHandle#bean. */
    private static final VarHandle VH_BEAN_HANDLE_BEAN = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanHandle.class, "bean", BeanSetup.class);

    /** The bean class, is typical void.class for functional beans. */
    public final Class<?> beanClass;

    /** The kind of bean. */
    public final BeanKind beanKind;

    /** The container this bean is installed in. */
    public final ContainerSetup container;

    /** The bean's injection manager. Null for functional beans, otherwise non-null */
    @Nullable
    public BeanInjectionManager injectionManager;

    /** The extension that installed the bean. */
    public final ExtensionSetup installedBy;

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** Supplies a mirror for the operation */
    public Supplier<? extends BeanMirror> mirrorSupplier;

    /** The name of this bean. Should only be updated through {@link #named(String)} */
    public String name;

    /** All beans in a container are maintained in a linked list. */
    @Nullable
    public BeanSetup nextBean;

    /** Operations declared by the bean. */
    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /** The beans lifetime operations. */
    public final List<LifetimeOperation> operationsLifetime = new ArrayList<>();

    public final List<ProvidedService> operationsProviders = new ArrayList<>();

    /** Non-null if the bean is owned by an extension. */
    @Nullable
    public final ExtensionSetup ownedBy;

    public boolean providingOperationsVisited;

    /** The assembly or extension used to install this component. */
    public final RealmSetup realm;

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, or an instance) */
    @Nullable
    public final Object source;

    /** The type of source the installer is created from. */
    public final BeanSourceKind sourceKind;

    /**
     * Create a new bean.
     */
    private BeanSetup(PackedBeanInstaller installer, BeanKind beanKind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source) {
        this.beanKind = requireNonNull(beanKind);
        this.beanClass = requireNonNull(beanClass);
        this.sourceKind = requireNonNull(sourceKind);
        this.source = source;

        ExtensionSetup installedBy = installer.useSite == null ? installer.beanExtension : installer.useSite.usedBy();

        RealmSetup realm = installer.useSite == null ? installer.beanExtension.container.assembly : installedBy.extensionRealm;

        this.installedBy = requireNonNull(installedBy);
        this.container = requireNonNull(installedBy.container);

        // I think we want to have a single field for these 2
        // I think this was made like this, when I was unsure if we could
        // have containers managed by extensions
        this.realm = requireNonNull(realm);
        if (realm instanceof ExtensionTreeSetup s) {
            this.ownedBy = installer.useSite.extension();
        } else {
            this.ownedBy = null;
        }

        ContainerLifetimeSetup cls = container.lifetime;
        if (beanKind == BeanKind.CONTAINER || beanKind == BeanKind.FUNCTIONAL || beanKind == BeanKind.STATIC) {
            this.lifetime = cls;
            cls.beans.add(this);
        } else {
            this.lifetime = new BeanLifetimeSetup(cls, this);
        }
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
        LifetimePoolOperationSetup os = new LifetimePoolOperationSetup(installedBy, this, OperationType.of(beanClass), injectionManager.accessBean(this));
        os.invocationType = (PackedInvocationType) os.invocationType.withReturnType(beanClass);
        return os;
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

    /** {@return the path of this component} */
    public NamespacePath path() {
        int size = container.depth;
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
        BeanHandle<?> handle = (BeanHandle<?>) VH_BEAN_CONFIGURATION_HANDLE.get(configuration);
        return crack(handle);
    }

    /**
     * Extracts a bean setup from a bean handle.
     * 
     * @param handle
     *            the handle to extract from
     * @return the bean setup
     */
    private static BeanSetup crack(BeanHandle<?> handle) {
        return (BeanSetup) VH_BEAN_HANDLE_BEAN.get(handle);
    }

    static BeanSetup install(PackedBeanInstaller installer, BeanKind beanKind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source,
            @Nullable BeanIntrospector introspector, @Nullable String namePrefix, boolean multiInstall, boolean synthetic) {
        BeanSetup bean = new BeanSetup(installer, beanKind, beanClass, sourceKind, source);

        ContainerSetup container = bean.container;

        String prefix = namePrefix;
        if (prefix == null) {
            prefix = "Functional";
            BeanModel beanModel = sourceKind == BeanSourceKind.NONE ? null : new BeanModel(beanClass);

            if (beanModel != null) {
                prefix = beanModel.simpleName();
            }
        }
        // TODO virker ikke med functional beans og naming
        String n = prefix;

        HashMap<Class<?>, Object> bcm = installer.beanExtension.container.beanClassMap;
        if (installer.useSite != null) {
            bcm = installer.useSite.usedBy().beanClassMap;

        }

        // if (installer.useSite.extension())

        if (beanClass != void.class) {
            if (multiInstall) {
                MuInst i = (MuInst) bcm.compute(beanClass, (c, o) -> {
                    if (o == null) {
                        return new MuInst();
                    } else if (o instanceof BeanSetup) {
                        throw new BeanClassAlreadyExistsException("Oops");
                    } else {
                        ((MuInst) o).counter += 1;
                        return o;
                    }
                });
                int next = i.counter;
                if (next > 0) {
                    n = prefix + next;
                }
                while (container.children.putIfAbsent(n, bean) != null) {
                    n = prefix + ++next;
                    i.counter = next;
                }
            } else {
                bcm.compute(beanClass, (c, o) -> {
                    if (o == null) {
                        return bean;
                    } else if (o instanceof BeanSetup) {
                        throw new BeanClassAlreadyExistsException("A non-multi bean has already been defined for " + bean.beanClass);
                    } else {
                        // We already have some multiple beans installed
                        throw new BeanClassAlreadyExistsException("Oops");
                    }
                });
                // Not multi install, so should be able to add it first time
                int size = 0;
                while (container.children.putIfAbsent(n, bean) != null) {
                    n = prefix + ++size;
                }
            }
        }
        bean.name = n;

        if (sourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.source;
            OperationSetup os = op.newOperationSetup(bean, bean.installedBy);
            bean.operations.add(os);
        }

        // Scan the bean class for annotations unless the bean class is void
        if (sourceKind != BeanSourceKind.NONE) {
            new IntrospectedBean(bean, introspector).introspect();
        }

        // Bean was successfully created, add it to the container
        BeanSetup siebling = container.beanLast;
        if (siebling == null) {
            container.beanFirst = bean;
        } else {
            siebling.nextBean = bean;
        }
        container.beanLast = bean;

        return bean;
    }
}

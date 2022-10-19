package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanExtension;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHandle.InstallOption;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.DublicateBeanClassException;
import app.packed.container.ExtensionPoint.UseSite;
import app.packed.operation.InvocationType;
import app.packed.operation.Op;
import app.packed.operation.OperationType;
import app.packed.operation.Provider;
import internal.app.packed.bean.BeanSetup.BeanInstallOption.CustomPrefix;
import internal.app.packed.bean.BeanSetup.BeanInstallOption.IntrospectWith;
import internal.app.packed.bean.BeanSetup.BeanInstallOption.MultiInstall;
import internal.app.packed.bean.BeanSetup.BeanInstallOption.Synthetic;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.NameCheck;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.oldservice.inject.BeanInjectionManager;
import internal.app.packed.operation.InvocationSite;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationTarget.BeanInstanceAccess;
import internal.app.packed.operation.op.PackedOp;
import internal.app.packed.operation.op.ReflectiveOp;
import internal.app.packed.service.ProvidedService;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.PackedNamespacePath;
import internal.app.packed.util.ThrowableUtil;

/** The internal configuration of a bean. */
public final class BeanSetup {

    /** Illegal bean classes. */
    private static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Key.class, Op.class, Optional.class, Provider.class);

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

    /** Non-null if the bean is installed for an extension. */
    @Nullable
    public final ExtensionSetup ownedByExtension;

    /** The bean's injection manager. Null for functional beans, otherwise non-null */
    @Nullable
    public final BeanInjectionManager injectionManager;

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

    @Nullable
    public Runnable onWiringAction;

    /** Operations declared by the bean. */
    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /** The realm used to install this component. */
    public final RealmSetup realm;

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, or an instance) */
    @Nullable
    public final Object source;

    /** The type of source the installer is created from. */
    public final BeanSourceKind sourceKind;

    public final List<LifetimeOp> lifetimeOperations = new ArrayList<>();

    public final List<ProvidedService> providingOperations = new ArrayList<>();

    public boolean providingOperationsVisited;

    /**
     * Create a new bean.
     */
    private BeanSetup(ExtensionSetup installedBy, BeanKind beanKind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source, RealmSetup realm,
            @Nullable ExtensionSetup extensionOwner) {
        this.installedBy = requireNonNull(installedBy);
        this.container = requireNonNull(installedBy.container);
        this.beanKind = requireNonNull(beanKind);
        this.beanClass = requireNonNull(beanClass);
        this.sourceKind = requireNonNull(sourceKind);
        this.source = source;


        // I think we want to have a single field for these 2
        // I think this was made like this, when I was unsure if we could
        // have containers managed by extensions
        this.realm = requireNonNull(realm);
        this.ownedByExtension = extensionOwner;

        ContainerLifetimeSetup cls = container.lifetime;
        if (beanKind == BeanKind.CONTAINER) {
            this.lifetime = cls;
            cls.beans.add(this);
        } else {
            this.lifetime = new BeanLifetimeSetup(cls, this);
        }

        this.injectionManager = new BeanInjectionManager(this);
    }

    // Relative to x
    public OperationSetup accessOperation() {
        // Hmm, er det med i listen af operationer???? IDK
        return new OperationSetup(this, OperationType.of(beanClass), new InvocationSite(InvocationType.raw(), installedBy), new BeanInstanceAccess(this, null),
                null);
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
    public static BeanSetup crack(BeanHandle<?> handle) {
        return (BeanSetup) VH_BEAN_HANDLE_BEAN.get(handle);
    }

    // Maaske lave vi kinds til et int flag. Hvis vi ogsaa skal tilfoeje only if absent

    // Eller maaske long term er det en record vi populere
    // install(F..withSource().ifAbsent)

    public static BeanSetup install(ExtensionSetup installedBy, BeanKind kind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source,
            @Nullable UseSite extensionUseSite, BeanHandle.InstallOption... options) {
        PackedExtensionPointContext c = ((PackedExtensionPointContext) extensionUseSite);
        RealmSetup r = extensionUseSite == null ? installedBy.container.assembly : c.extension().extensionRealm;
        return install(kind, beanClass, sourceKind, source, installedBy, r, extensionUseSite == null ? null : c.extension(), options);
    }

    // Maybe move to bean extension???
    static BeanSetup install(BeanKind kind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source, ExtensionSetup installedBy,
            RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanHandle.InstallOption... options) {
        if (ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot register a bean with bean class " + beanClass);
        }

        BeanSetup bean = new BeanSetup(installedBy, kind, beanClass, sourceKind, source, realm, extensionOwner);

        // No reason to maintain some of these in props
        boolean multiInstall = false;
        BeanIntrospector customIntrospector = null;
        String prefix = null;

        // Process each option
        requireNonNull(options, "options is null");
        for (InstallOption o : options) {
            requireNonNull(o, "option was null");
            if (o instanceof BeanInstallOption.IntrospectWith ci) {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("Custom Introspector cannot be used with functional or static beans");
                }
                customIntrospector = ci.introspector();
            } else if (o instanceof BeanInstallOption.CustomPrefix cp) {
                prefix = cp.prefix();
            } else {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("NonUnique cannot be used with functional or static beans");
                }
                multiInstall = true;
            }
        }

        BeanModel beanModel = sourceKind == BeanSourceKind.NONE ? null : new BeanModel(beanClass);

        ContainerSetup container = bean.container;

        if (prefix == null) {
            prefix = "Functional";
            if (beanModel != null) {
                prefix = beanModel.simpleName();
            }
        }
        // TODO virker ikke med functional beans og naming
        String n = prefix;

        if (beanClass != void.class) {
            if (multiInstall) {
                class MuInst {
                    int counter;
                }
                MuInst i = (MuInst) bean.container.beanClassMap.compute(beanClass, (c, o) -> {
                    if (o == null) {
                        return new MuInst();
                    } else if (o instanceof BeanSetup) {
                        throw new DublicateBeanClassException("Oops");
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
                bean.container.beanClassMap.compute(beanClass, (c, o) -> {
                    if (o == null) {
                        return bean;
                    } else if (o instanceof BeanSetup) {
                        throw new DublicateBeanClassException("A non-multi bean has already been defined for " + bean.beanClass);
                    } else {
                        // We already have some multiple beans installed
                        throw new DublicateBeanClassException("Oops");
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

        boolean packedInstantiates = kind.hasInstances() && sourceKind != BeanSourceKind.INSTANCE;

        if (packedInstantiates) {

            // Vi skal have noget generelt support for POPs
            // Bruger dem ogsaa i OnBindings

            PackedOp<?> op;
            if (bean.sourceKind == BeanSourceKind.CLASS) {
                op = ReflectiveOp.DEFAULT_FACTORY.get((Class<?>) bean.source);
            } else {
                op = (PackedOp<?>) bean.source; // We always unpack source Op to PackedOp
            }

            // Extract a MethodHandlefrom the factory
            MethodHandle mh = bean.realm.beanAccessor().toMethodHandle(op);

            OperationType type = op.type();
            // Create an instantiating operation
            ExtensionSetup es = container.useExtensionSetup(BeanExtension.class, null);

            // Passer jo ikke...

            // pop skal lave en operationSetup????
            OperationSetup os = new OperationSetup(bean, type, new InvocationSite(InvocationType.raw(), es), new BeanInstanceAccess(bean, mh), null);
            bean.operations.add(os);
        }

        // Scan the bean class for annotations unless the bean class is void or is from a java package
        if (sourceKind != BeanSourceKind.NONE && bean.beanClass.getModule() != Introspector.JAVA_BASE_MODULE) {
            new Introspector(beanModel, bean, customIntrospector).introspect();
        }

        // Maintain some tree logic
        // Maybe we need to move this up
        BeanSetup siebling = container.beanLast;
        if (siebling == null) {
            container.beanFirst = bean;
        } else {
            siebling.nextBean = bean;
        }
        container.beanLast = bean;

        return bean;
    }

    public static BeanSetup installClass(ExtensionSetup installedBy, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind beanKind,
            Class<?> clazz, BeanHandle.InstallOption... options) {
        requireNonNull(clazz, "clazz is null");
        return install(beanKind, clazz, BeanSourceKind.CLASS, clazz, installedBy, realm, extensionOwner, options);
    }

    public static BeanSetup installFunctional(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner,
            BeanHandle.InstallOption... options) {
        return install(BeanKind.FUNCTIONAL, void.class, BeanSourceKind.NONE, null, operator, realm, extensionOwner, options);
    }

    public static BeanSetup installInstance(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, Object instance,
            BeanHandle.InstallOption... options) {
        requireNonNull(instance, "instance is null");
        return install(BeanKind.CONTAINER, instance.getClass(), BeanSourceKind.INSTANCE, instance, operator, realm, extensionOwner, options);
    }

    public static BeanSetup installOp(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind beanKind, Op<?> op,
            BeanHandle.InstallOption... options) {
        PackedOp<?> pop = PackedOp.crack(op);
        return install(beanKind, pop.type().returnType(), BeanSourceKind.OP, pop, operator, realm, extensionOwner, options);
    }

    /** The various bean install options. */
    // Silly Eclipse compiler requires permits here (bug)
    public sealed interface BeanInstallOption extends BeanHandle.InstallOption permits MultiInstall, IntrospectWith, CustomPrefix, Synthetic {

        /**
         * Allows for a custom introspector.
         * 
         * @see InstallOption#introspectWith(BeanIntrospector)
         */
        public record IntrospectWith(BeanIntrospector introspector) implements BeanInstallOption {}

        /**
         * Allows for multi install of a bean.
         * 
         * @see InstallOption#multiInstall()
         */
        public final class MultiInstall implements BeanInstallOption {}

        public final class Synthetic implements BeanInstallOption {} // maybe this is only for packed, in which case a flag I think

        public record CustomPrefix(String prefix) implements BeanInstallOption {}
    }
}

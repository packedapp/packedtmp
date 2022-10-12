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
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHandle.InstallOption;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.MultipleBeanOfSameTypeDefinedException;
import app.packed.operation.Op;
import app.packed.operation.Provider;
import internal.app.packed.bean.BeanSetup.BeanInstallOption.CustomIntrospector;
import internal.app.packed.bean.BeanSetup.BeanInstallOption.CustomPrefix;
import internal.app.packed.bean.BeanSetup.BeanInstallOption.MultiInstall;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.NameCheck;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.op.PackedOp;
import internal.app.packed.service.inject.BeanInjectionManager;
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
    public final ExtensionSetup extensionOwner;

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

    /**
     * Create a new bean.
     */
    private BeanSetup(BeanKind beanKind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source, ExtensionSetup operator, RealmSetup realm,
            @Nullable ExtensionSetup extensionOwner) {
        this.beanKind = requireNonNull(beanKind);
        this.beanClass = requireNonNull(beanClass);
        this.sourceKind = requireNonNull(sourceKind);
        this.source = source;

        this.installedBy = requireNonNull(operator);
        this.container = requireNonNull(operator.container);

        // TODO clean up
        
        // I think we want to have a single field for these 2
        // I think this was made like this, when I was unsure if we could
        // have containers managed by extensions
        this.realm = requireNonNull(realm);
        this.extensionOwner = extensionOwner;

        this.lifetime = container.lifetime;

        this.injectionManager = new BeanInjectionManager(this);
    }

    public void checkIsCurrent() {
        if (!realm.isCurrent(this)) {
            String errorMsg;
            // if (realm.container == this) {
            errorMsg = "This operation must be called as the first thing in Assembly#build()";
            // } else {
            // errorMsg = "This operation must be called immediately after the component has been wired";
            // }
            // is it just named(), in that case we should say it explicityly instead of just saying "this operation"
            throw new IllegalStateException(errorMsg);
        }
    }

    /** {@return a new mirror.} */
    public BeanMirror mirror() {

        // Create a new BeanMirror
        BeanMirror mirror;
        if (mirrorSupplier == null) {
            mirror = new BeanMirror();
        } else {
            mirror = mirrorSupplier.get();
            if (mirror == null) {
                throw new NullPointerException(mirrorSupplier + " returned a null instead of an " + BeanMirror.class.getSimpleName() + " instance");
            }
        }

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
        checkIsCurrent();

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
        paths[size + 1] = name;
        ContainerSetup c = container;
        for (int i = size; i >= 0; i--) {
            paths[i] = c.name;
            c = c.treeParent;
        }
        return new PackedNamespacePath(paths);
    }

    public static BeanSetup crack(BeanConfiguration configuration) {
        BeanHandle<?> handle = (BeanHandle<?>) VH_BEAN_CONFIGURATION_HANDLE.get(configuration);
        return crack(handle);
    }

    public static BeanSetup crack(BeanHandle<?> handle) {
        return (BeanSetup) VH_BEAN_HANDLE_BEAN.get(handle);
    }

    static BeanSetup install(BeanKind kind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source, ExtensionSetup installedBy,
            RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanHandle.InstallOption... options) {
        if (ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot register a bean with bean class " + beanClass);
        }

        BeanSetup bean = new BeanSetup(kind, beanClass, sourceKind, source, installedBy, realm, extensionOwner);

        // No reason to maintain some of these in props
        boolean multiInstall = false;
        BeanIntrospector customIntrospector = null;
        String prefix = null;

        // Process each option
        requireNonNull(options, "options is null");
        for (InstallOption o : options) {
            requireNonNull(o, "option was null");
            if (o instanceof BeanInstallOption.CustomIntrospector ci) {
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

        realm.wireCurrentComponent();

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
                        throw new MultipleBeanOfSameTypeDefinedException();
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
                        throw new MultipleBeanOfSameTypeDefinedException("A non-multi bean has already been defined for " + bean.beanClass);
                    } else {
                        // We already have some multiple beans installed
                        throw new MultipleBeanOfSameTypeDefinedException();
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

        realm.wireNew(bean);

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

    public static BeanSetup installClass(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind beanKind, Class<?> clazz,
            BeanHandle.InstallOption... options) {
        requireNonNull(clazz, "clazz is null");
        return install(beanKind, clazz, BeanSourceKind.CLASS, clazz, operator, realm, extensionOwner, options);
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

    /** The various install options. */
    // Silly Eclipse compiler requires permits here (bug)
    public sealed interface BeanInstallOption extends BeanHandle.InstallOption permits MultiInstall, CustomIntrospector, CustomPrefix {

        public record MultiInstall() implements BeanInstallOption {}

        public record CustomIntrospector(BeanIntrospector introspector) implements BeanInstallOption {}

        public record CustomPrefix(String prefix) implements BeanInstallOption {}
    }
}

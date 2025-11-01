package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHook;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanLocal.Accessor;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.lifecycle.LifecycleModel;
import app.packed.binding.Key;
import app.packed.build.hook.BuildHook;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.context.Context;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.util.Nullable;
import internal.app.packed.bean.scanning.BeanScanner;
import internal.app.packed.binding.BindingProvider;
import internal.app.packed.binding.BindingProvider.FromCodeGeneratedConstant;
import internal.app.packed.binding.BindingProvider.FromConstant;
import internal.app.packed.binding.BindingProvider.FromLifetimeArena;
import internal.app.packed.binding.BindingProvider.FromOperationResult;
import internal.app.packed.binding.BindingProvider.FromSidebeanLifetimeArena;
import internal.app.packed.binding.SuppliedBindingKind;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedComponentSetup;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.lifecycle.LifecycleOperationHandle.FactoryOperationHandle;
import internal.app.packed.lifecycle.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifecycle.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifecycle.lifetime.LifetimeSetup;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreEntry;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;
import internal.app.packed.operation.BeanOperationStore;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.PackedOp.NewOperation;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.service.MainServiceNamespaceHandle;
import internal.app.packed.service.ServiceProviderSetup.BeanServiceProviderSetup;
import internal.app.packed.service.util.ServiceMap;
import internal.app.packed.util.accesshelper.BeanAccessHandler;
import internal.app.packed.util.accesshelper.BeanScanningAccessHandler;

/** The internal configuration of a bean. */
public final class BeanSetup implements ContextualizedComponentSetup, BuildLocalSource, ComponentSetup, LifetimeStoreEntry {

    public final HashMap<PackedBeanAttachmentKey, PackedAttachmentOperationHandle> attachments = new HashMap<>();

    /** The kind of bean. */
    public final BeanLifetime beanKind;

    /** The lifecycle kind of the bean. */
    public final LifecycleModel beanLifecycleKind = LifecycleModel.UNMANAGED_LIFECYCLE;

    /** Services that have been bound specifically for this bean. */
    public final ServiceMap<BeanServiceProviderSetup> serviceProviders = new ServiceMap<>();

    /** The container this bean is installed in. */
    public final ContainerSetup container;

    /** Contexts the bean are in. */
    private final HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /** The bean's handle. */
    @Nullable
    private BeanHandle<?> handle;

    /** The extension that installed the bean. */
    public final ExtensionSetup installedBy;

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** An index into a lifetime store, or null if transient. */
    @Nullable
    public final LifetimeStoreIndex lifetimeStoreIndex;

    public int multiInstall;

    /** The name of this bean. Should only be updated by {@link internal.app.packed.container.ContainerBeanStore}. */
    String name;

    /** The operations of this bean. */
    public final BeanOperationStore operations = new BeanOperationStore();

    /** The owner of the bean. */
    public final AuthoritySetup<?> owner;

    @Nullable
    public BeanScanner scanner;

    /** The registered bean. */
    public final PackedBean<?> bean;

    /** The bean's template. */
    public final PackedBeanTemplate template;

    public final ArrayList<PackedSideBeanUsage> sideBeans = new ArrayList<>();

    /** Create a new bean. */
    private BeanSetup(PackedBeanInstaller installer, PackedBean<?> bean) {
        this.beanKind = requireNonNull(installer.template.beanKind());
        this.bean = requireNonNull(bean);

        this.template = installer.template;
        this.container = requireNonNull(installer.installledByExtension.container);
        this.installedBy = requireNonNull(installer.installledByExtension);
        this.owner = requireNonNull(installer.owner);

        ContainerLifetimeSetup containerLifetime = container.lifetime;
        if (beanKind == BeanLifetime.SINGLETON) {
            this.lifetime = containerLifetime;
            this.lifetimeStoreIndex = container.lifetime.addBean(this);
        } else {
            BeanLifetimeSetup bls = new BeanLifetimeSetup(this);
            this.lifetime = bls;
            this.lifetimeStoreIndex = null;
            // containerLifetime.addDetachedChildBean(bls);
        }

        if (bean.beanSourceKind != BeanSourceKind.SOURCELESS) {
            scanner = new BeanScanner(this);
        }

        for (PackedContextTemplate t : installer.template.initializationTemplate().contexts) {
            contexts.put(t.contextClass(), new ContextSetup(t, this));
        }
    }

    public PackedAttachmentOperationHandle attach(Class<? extends Extension<?>> extension, Op<?> op, boolean ifAbsent) {
        Key<?> key = op.type().returnVariable().toKey();
        return attachments.compute(new PackedBeanAttachmentKey(extension, key), (_, v) -> {
            if (v == null) {

            } else if (ifAbsent) {
                return v;
            }
            throw new IllegalArgumentException("An attachment for the specified key has already been registered, key=" + key);
        });
    }

    public BindingProvider beanInstanceBindingProvider() {
        if (bean.beanSourceKind == BeanSourceKind.INSTANCE) {
            return new FromConstant(bean.beanSource.getClass(), bean.beanSource);
        } else if (beanKind == BeanLifetime.SINGLETON) { // we've already checked if instance
            return new FromLifetimeArena(container.lifetime, lifetimeStoreIndex, bean.beanClass);
        } else if (beanKind == BeanLifetime.SIDEBEAN) {
            return new FromSidebeanLifetimeArena(bean.beanClass);
        } else if (beanKind == BeanLifetime.UNMANAGED) {
            return new FromOperationResult(operations.first());
        } else {
            throw new Error();
        }
    }

    public <K> void bindCodeGeneratedConstant(Key<K> key, Supplier<? extends K> supplier) {
        requireNonNull(supplier, "supplier is null");
        serviceProviders.put(key, new BeanServiceProviderSetup(key, new FromCodeGeneratedConstant(supplier, SuppliedBindingKind.CODEGEN)));
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
        return cs;
    }

    /** {@inheritDoc} */
    @Override
    public void forEachContext(Consumer<? super ContextSetup> action) {
        contexts.values().forEach(action);
    }

    /** {@return the bean's handle} */
    @Override
    public BeanHandle<?> handle() {
        return requireNonNull(handle);
    }

    public Iterable<BuildHook> hooks() {
        return Collections.emptyList();
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

    /** {@return the name of the bean} */
    public String name() {
        return name;
    }

    // I think we name beans BaseExtension#main
    // I think we name beans SchedulingExtension#main
    public void named(String newName) {
        container.beans.updateBeanName(this, newName);
    }

    /** {@return the owner of the bean} */
    public ComponentRealm owner() {
        return owner.authority();
    }

    public MainServiceNamespaceHandle serviceNamespace() {
        if (owner instanceof ExtensionSetup es) {
            return es.services();
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
        case BeanHandle<?> b -> crack(b);
        case BeanIntrospector<?> b -> crack(b);
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
        return crack(BeanAccessHandler.instance().getBeanConfigurationHandle(configuration));
    }

    public static BeanSetup crack(BeanHandle<?> handle) {
        return BeanAccessHandler.instance().getBeanHandleBean(handle);
    }

    public static BeanSetup crack(BeanIntrospector<?> introspector) {
        return BeanScanningAccessHandler.instance().invokeBeanIntrospectorBean(introspector);
    }

    public static BeanSetup crack(BeanMirror mirror) {
        return crack(BeanAccessHandler.instance().getBeanMirrorHandle(mirror));
    }

    /**
     * Installs a new bean.
     *
     * @param <H>
     *            the type of bean handle that is created to represent the bean
     * @param installer
     *            the bean installer used to install the bean
     * @param bean
     *            the bean that is being installed
     * @param handleFactory
     *            a function responsible for creating the bean's handle
     * @return a handle for the new bean
     */
    @SuppressWarnings("unchecked")
    static <H extends BeanHandle<?>> H newBean(PackedBeanInstaller installer, PackedBean<?> bean, Function<? super BeanInstaller, H> handleFactory) {
        requireNonNull(bean, "bean is null");
        requireNonNull(handleFactory, "handleFactory is null");

        if (!installer.owner.isConfigurable()) {
            throw new IllegalStateException();
        }

        installer.checkNotUsed();

        // TODO Apply preBean Hooks
        
        // Create the internal bean
        BeanSetup newBean = new BeanSetup(installer, bean);

        // Install it, this also marks this installer as unconfigurable
        newBean = installer.install(newBean);

        // Create a handle for the new bean
        BeanHandle<?> handle = newBean.handle = handleFactory.apply(installer);

        // We need to install a factory operation, if an Op was specified when creating the bean.
        checkForFactoryOp(newBean);

        // Scan the bean if needed
        BeanScanner scanner = newBean.scanner;
        if (scanner != null) {
            scanner.introspect();
            newBean.scanner = null;
        }

        // Add the bean to the container and initialize the name of the bean
        newBean.container.beans.installAndSetBeanName(newBean, installer.namePrefix);

        // Apply hooks to the bean. Hmm, I think may have hooks we need to apply before scanning
        newBean.container.assembly.model.hooks.forEach(BeanHook.class, h -> h.onNew(handle.configuration()));

        // Return the handle of the new bean
        return (H) handle;
    }

    static void checkForFactoryOp(BeanSetup bean) {
        // Creating an bean factory operation representing the Op if an Op was specified when creating the bean.
        if (bean.bean.beanSourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.bean.beanSource;

            PackedOperationTemplate ot = bean.template.initializationTemplate();
            // if (ot.returnKind == ReturnKind.DYNAMIC) {
            // ot = ot.configure(c -> c.returnType(beanClass));
            // }

//            ot = bean.template.initializationTemplate()

            // What if no scanning and OP?????
            op.newOperationSetup(new NewOperation(bean, bean.installedBy, ot, i -> new FactoryOperationHandle(i), null));
        }

    }
}

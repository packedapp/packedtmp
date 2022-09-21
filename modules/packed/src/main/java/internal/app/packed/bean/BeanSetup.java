package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.stream.Stream;

import app.packed.base.Nullable;
import app.packed.bean.BeanExtension;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.container.Extension;
import app.packed.container.UserOrExtension;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.bindings.BeanInjectionManager;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The build-time configuration of a bean. */
public sealed class BeanSetup extends ComponentSetup implements BeanInfo permits ExtensionBeanSetup {

    /** A MethodHandle for invoking {@link BeanMirror#initialize(BeanSetup)}. */
    private static final MethodHandle MH_BEAN_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BeanMirror.class, "initialize",
            void.class, BeanSetup.class);

    /** A model of hooks on the bean class. Or null if no member scanning was performed. */
    @Nullable
    public final BeanClassModel beanModel;

    /** The installer that was used to create the bean. */
    public final PackedBeanHandleInstaller<?> installer;

    /** The bean's injection manager. Null for functional beans, otherwise non-null */
    @Nullable
    public final BeanInjectionManager injectionManager;

    /** Operations declared by the bean. */
    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /**
     * Create a new bean setup.
     * 
     * @param builder
     *            the handle builder
     */
    public BeanSetup(PackedBeanHandleInstaller<?> builder, RealmSetup owner) {
        super(builder.container.application, owner, builder.container);
        this.installer = builder;
        this.beanModel = builder.sourceKind == BeanSourceKind.NONE ? null : new BeanClassModel(builder.beanClass);// realm.accessor().beanModelOf(driver.beanClass());
        if (builder.beanClass != void.class) { // Not sure exactly when we need it
            this.injectionManager = new BeanInjectionManager(this, builder);
        } else {
            this.injectionManager = null;
        }

        // Wire the hook model
        if (beanModel != null) {
            // hookModel.onWire(this);

            // Set the name of the component if it have not already been set using a wirelet
            initializeNameWithPrefix(beanModel.simpleName());
        }
    }
    
    final void initializeNameWithPrefix0(String name) {
        initializeNameWithPrefix(name);
    }
    public void addOperation(OperationSetup operation) {
        operations.add(requireNonNull(operation));
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> beanClass() {
        return installer.beanClass;
    }

    /** {@return a new mirror.} */
    public BeanMirror mirror() {
        // Create a new BeanMirror
        BeanMirror mirror = installer.mirrorSupplier.get();
        if (mirror == null) {
            throw new NullPointerException(installer.mirrorSupplier + " returned a null instead of an " + BeanMirror.class.getSimpleName() + " instance");
        }

        // Initialize BeanMirror by calling BeanMirror#initialize(BeanSetup)
        try {
            MH_BEAN_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> operator() {
        return installer.operator == null ? BeanExtension.class : installer.operator.extension().extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public UserOrExtension owner() {
        return realm.realm();
    }

    @Override
    public Stream<ComponentSetup> stream() {
        return Stream.of(this);
    }
}

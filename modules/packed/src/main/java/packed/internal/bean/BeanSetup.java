package packed.internal.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.operation.OperationMirror;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import packed.internal.bean.PackedBeanDriver.SourceType;
import packed.internal.bean.hooks.usesite.HookModel;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.RealmSetup;
import packed.internal.inject.manager.InjectionManager;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** The build-time configuration of a bean. */
public final class BeanSetup extends ComponentSetup {

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_CONTAINER_CONFIGURATION_ON_WIRE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class,
            "onWired", void.class);

    /** The driver used to create a bean. */
    public final PackedBeanDriver<?> driver;

    /** A model of the hooks on the bean. */
    @Nullable
    public final HookModel hookModel;

    /** Manages injection for bean. */
    @Nullable
    private InjectionManager injectionManager;

    public final BeanSetupTmp bs;
    
    public BeanSetup(ContainerSetup container, RealmSetup realm, PackedBeanDriver<?> driver) {
        super(container.application, realm, container);

        this.driver = driver;
        this.hookModel = driver.sourceType == SourceType.NONE ? null : realm.accessor().beanModelOf(driver.beanType);
        
        this.bs = new BeanSetupTmp(this);
        
        // Can only register a single extension bean of a particular type
        if (driver.extension != null && driver.beanKind() == BeanKind.CONTAINER) {
            driver.extension.injectionManager.addBean(driver, this);
        }

        // Find a hook model for the bean type and wire it
        if (hookModel != null) {
            hookModel.onWire(this);

            // Set the name of the component if it have not already been set using a wirelet
            initializeNameWithPrefix(hookModel.simpleName());
        }
    }

    public InjectionManager injectionManager() {
        InjectionManager m = injectionManager;
        if (m == null) {
            ExtensionSetup extension = driver.extension;
            if (extension == null) {

            } else {
                m = injectionManager = extension.injectionManager;
            }
        }
        return m;
    }

    /** {@inheritDoc} */
    @Override
    public BeanMirror mirror() {
        return new BuildTimeBeanMirror();
    }

    /** {@inheritDoc} */
    @Override
    public void onWired() {
        try {
            MH_CONTAINER_CONFIGURATION_ON_WIRE.invokeExact((ComponentConfiguration) driver.configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        super.onWired();
    }

    /** A build-time bean mirror. */
    public final class BuildTimeBeanMirror extends AbstractBuildTimeComponentMirror implements BeanMirror {

        /** {@inheritDoc} */
        @Override
        public Class<?> beanClass() {
            return hookModel.clazz;
        }

        /** {@inheritDoc} */
        @Override
        public BeanKind beanKind() {
            return driver.beanKind();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<ComponentMirror> children() {
            return List.of();
        }

        /** {@inheritDoc} */
        public final ContainerMirror container() {
            return parent.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Class<? extends Extension<?>>> registrant() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<OperationMirror> operations() {
            return driver.operations.toMirrors();
        }
    }
}

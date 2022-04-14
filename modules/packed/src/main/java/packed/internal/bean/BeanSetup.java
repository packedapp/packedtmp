package packed.internal.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.hooks.BeanInfo;
import app.packed.bean.operation.mirror.OperationMirror;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentMirror;
import app.packed.component.Realm;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.lifetime.LifetimeMirror;
import packed.internal.bean.PackedBeanHandleBuilder.SourceType;
import packed.internal.bean.hooks.BeanScanner;
import packed.internal.component.ComponentSetup;
import packed.internal.component.ComponentSetupRelation;
import packed.internal.inject.BeanInjectionManager;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** The build-time configuration of a bean. */
public final class BeanSetup extends ComponentSetup implements BeanInfo {

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_CONTAINER_CONFIGURATION_ON_WIRE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class,
            "onWired", void.class);

    /** The builder that was used to create the bean. */
    public final PackedBeanHandleBuilder<?> builder;

    final ArrayList<BeanConfiguration> configurations = new ArrayList<>(1);

    /** A model of the hooks on the bean. */
    @Nullable
    public final BaseHookModel hookModel;

    /** The bean's injection manager. */
    public final BeanInjectionManager injectionManager;

    /** Manages the operations defined by the bean. */
    public final BeanOperationManager operations = new BeanOperationManager();
    
    public BeanSetup(PackedBeanHandleBuilder<?> builder) {
        super(builder.container.application, builder.realm, builder.container);
        this.builder = builder;
        this.hookModel = builder.sourceType == SourceType.NONE ? null : new BaseHookModel(builder.beanClass());// realm.accessor().beanModelOf(driver.beanClass());
        this.injectionManager = new BeanInjectionManager(this, builder);

        if (builder.sourceType != SourceType.NONE) {
            new BeanScanner(this, builder.beanClass()).scan();
        }

        // Wire the hook model
        if (hookModel != null) {
            // hookModel.onWire(this);

            // Set the name of the component if it have not already been set using a wirelet
            initializeNameWithPrefix(hookModel.simpleName());
        }
    }


    /** {@inheritDoc} */
    public BeanSetup addConfiguration(BeanConfiguration configuration) {
        requireNonNull(configuration);
        configurations.add(configuration);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> beanClass() {
        return builder.beanClass();
    }

    /** {@inheritDoc} */
    @Override
    public BeanKind beanKind() {
        return builder.beanKind();
    }

    /** {@inheritDoc} */
    @Override
    public BuildTimeBeanMirror mirror() {
        return new BuildTimeBeanMirror(this);
    }

    /** {@inheritDoc} */
    @Override
    public void onWired() {
        for (ComponentConfiguration cc : configurations) {
            try {
                MH_CONTAINER_CONFIGURATION_ON_WIRE.invokeExact(cc);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
        super.onWired();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> operator() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Realm owner() {
        return builder.realm.realm();
    }

    @Override
    public Stream<ComponentSetup> stream() {
        return Stream.of(this);
    }
    

    /** A build-time bean mirror. */
    public record BuildTimeBeanMirror(BeanSetup bean) implements BeanMirror {

        /** {@inheritDoc} */
        @Override
        public Class<?> beanClass() {
            return bean.builder.beanClass();
        }

        /** {@inheritDoc} */
        @Override
        public BeanKind beanKind() {
            return bean.builder.beanKind();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<ComponentMirror> children() {
            return List.of();
        }

        /** {@inheritDoc} */
        public final ContainerMirror container() {
            return bean.parent.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Class<? extends Extension<?>>> registrant() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<OperationMirror> operations() {
            return bean.operations.toMirrorsStream();
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror application() {
            return bean.application.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public AssemblyMirror assembly() {
            return bean.assembly.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<ComponentMirror> stream() {
            return Stream.of(this);
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return bean.depth;
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeMirror lifetime() {
            return bean.lifetime.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return bean.name;
        }

        /** {@inheritDoc} */
        @Override
        public Realm owner() {
            return bean.realm.realm();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<ContainerMirror> parent() {
            return Optional.of(bean.parent.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public NamespacePath path() {
            return bean.path();
        }

        /** {@inheritDoc} */
        @Override
        public Relation relationTo(ComponentMirror other) {
            requireNonNull(other, "other is null");
            return ComponentSetupRelation.of(bean, ComponentSetup.crackMirror(other));
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return bean.toString();
        }
    }
}

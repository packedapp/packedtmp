package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.OldApplicationPath;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Operative;
import app.packed.context.Context;
import app.packed.context.ContextMirror;
import app.packed.context.ContextScopeMirror;
import app.packed.context.ContextualizedElementMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMetaHook.BindingTypeHook;
import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationMirror;
import app.packed.service.mirror.ServiceProviderMirror;
import app.packed.service.mirror.oldMaybe.OverriddenServiceBindingMirror;
import app.packed.util.Key;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.operation.OperationSetup;
import sandbox.operation.mirror.DependenciesMirror;

/**
 * A mirror of a bean.
 * <p>
 * An instance of BeanMirror can be injected at runtime simply by declaring a dependency on it.
 */
@BindingTypeHook(extension = BaseExtension.class)
public non-sealed class BeanMirror implements BeanLocalAccessor, ComponentMirror, ContextualizedElementMirror , ContextScopeMirror , ServiceProviderMirror {

    /** The bean we are mirroring. */
    final BeanSetup bean;

    /**
     * Create a new bean mirror.
     *
     * @throws IllegalStateException
     *             if attempting to explicitly construct a bean mirror instance
     */
    public BeanMirror() {
        // Will fail if the bean mirror is not initialized by the framework
        this.bean = BeanSetup.MIRROR_INITIALIZER.initialize();
    }

    /** {@return the application the bean is a part of.} */
    public ApplicationMirror application() {
        return bean.container.application.mirror();
    }

    /** {@return the assembly where the bean's container is defined.} */
    public AssemblyMirror assembly() {
        // Extension beans can be defined from any container where extension is used
        // However, we always return the assembly of the container in which it is registered
        return bean.container.assembly.mirror();
    }

    /**
     * Returns the type (class) of the bean.
     * <p>
     * Beans that do not have a proper class, for example, a {@link BeanKind#FUNCTIONAL functional} bean. Will have
     * {@code void.class} as their bean class.
     *
     * @return the type (class) of the bean.
     */
    public final Class<?> beanClass() {
        return bean.beanClass;
    }

    /** {@return the bean kind} */
    public final BeanKind beanKind() {
        return bean.beanKind;
    }

    /** {@return the bean source kind} */
    public final BeanSourceKind beanSourceKind() {
        return bean.beanSourceKind;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return bean.componentPath();
    }

    /** {@return the container the bean belongs to.} */
    public ContainerMirror container() {
        return bean.container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Class<? extends Context<?>>, ContextMirror> contexts() {
        return ContextSetup.allMirrorsFor(bean);
    }

    /** {@return the dependencies this bean introduces.} */
    @SuppressWarnings("exports") // uses sandbox classes
    public DependenciesMirror dependencies() {
        return new BeanDependenciesMirror(bean);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof BeanMirror m && bean == m.bean;
    }

    /**
     * If instances of this bean is created at runtime. This method will return the operation that creates the instance.
     *
     * @return operation that creates instances of the bean. Or empty if instances are never created
     */
    // instantiatedBy

    // Syntes maaske bare skal lede efter den i operations()?
    // Saa supportere vi ogsaa flere factory metodes hvis vi har brug for det en gang
    // We don't support multi factory for default installs.
    // However custom bean templates may support it
    public Optional<OperationMirror> factoryOperation() {
        if (bean.beanKind != BeanKind.STATIC && bean.beanSourceKind != BeanSourceKind.INSTANCE) {
            return Optional.of(bean.operations.get(0).mirror());
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return bean.hashCode();
    }

    /**
     * Returns the bean's lifetime.
     * <p>
     * This is either a {@link ContainerLifetimeMirror} if a single instance of the bean is created together with the
     * container instance. Or if a functional or static bean.
     * <p>
     * A lazy bean or prototype bean will return
     *
     * @return the bean's lifetime
     */
    public LifetimeMirror lifetime() {
        return bean.lifetime.mirror();
    }

    public Collection<LifetimeMirror> managesLifetimes() {
        // Find LifetimeOperations->Unique on Lifetime
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the name of this bean.
     * <p>
     * If no name was explicitly set when the bean was configured. The framework will automatically assign an unique name to
     * it.
     *
     * @return the name of this bean
     */
    public final String name() {
        return bean.name();
    }

    /** {@return a stream of all of the operations declared by the bean.} */
    public Stream<OperationMirror> operations() {
        return bean.operations.stream().map(OperationSetup::mirror);
    }

    /**
     * Returns a stream of all of the operations declared by the bean with the specified mirror type.
     *
     * @param <T>
     * @param operationType
     *            the type of operations to include
     * @return a collection of all of the operations declared by the bean of the specified type.
     */
    @SuppressWarnings("unchecked")
    public <T extends OperationMirror> Stream<T> operations(Class<T> operationType) {
        requireNonNull(operationType, "operationType is null");
        return (Stream<T>) operations().filter(f -> operationType.isAssignableFrom(f.getClass()));
    }

    /**
     * Returns any extension the bean's driver is part of. All drivers are either part of an extension. Or is a build in
     * drive
     * <p>
     * Another thing is extension member, which is slightly different.
     *
     * @return any extension the bean's driver is part of
     */
    // handledBy, managedBy (What if the bean is unmanaged, or stateless)
    // Think it should be similar named on the operation
    Class<? extends Extension<?>> operator() { // registrant
        return bean.installedBy.extensionType;
    }

    @SuppressWarnings("exports")
    public Map<Key<?>, Collection<OverriddenServiceBindingMirror>> overriddenServices() {
        throw new UnsupportedOperationException();
    }

    /** {@return the owner of the bean.} */
    // declaredBy
    public final Operative owner() {
        return bean.author();
    }

    public OldApplicationPath path() {
        return bean.path();
    }

    /**
     * @param to
     *            the bean to return a relationship mirror to
     * @return a bean relationship mirror to the
     */
    public final Relationship relationshipTo(BeanMirror to) {
        requireNonNull(to, "to is null");
        BeanSetup other = to.bean;
        if (bean.container.application.deployment != other.container.application.deployment) {
            throw new IllegalArgumentException("The specified bean is not part of the same deployment as this bean");
        }
        return new Relationship(bean, other);
    }

    private record BeanDependenciesMirror(BeanSetup bean) implements DependenciesMirror {

        /** {@inheritDoc} */
        @Override
        public Collection<BeanMirror> beans() {
            HashSet<BeanSetup> set = new HashSet<>();
            for (OperationSetup os : bean.operations) {
                os.forEachBinding(b -> {
                    throw new UnsupportedOperationException();
                });
            }
            return set.stream().map(s -> s.mirror()).collect(Collectors.toSet());
        }

        /** {@inheritDoc} */
        @Override
        public Collection<ContainerMirror> containers() {
            // What if something is provided by an extension bean in the root container
            // doesn't make to say we depend on such a container

            // maybe we have extensionBeans() as a seperate method???
            // and beans are beans in the same realm...
            return beans().stream().map(b -> b.container()).distinct().toList();
        }

        /** {@inheritDoc} */
        @Override
        public Set<Class<? extends Extension<?>>> extensions() {
            HashSet<Class<? extends Extension<?>>> set = new HashSet<>();
            for (OperationSetup os : bean.operations) {
                os.forEachBinding(b -> {
                    if (b.boundBy.isExtension()) {
                        if (b.boundBy != bean.author()) {
                            set.add((b.boundBy.extension()));
                        }
                    }
                });
            }
            return Set.copyOf(set);
        }

        /** {@inheritDoc} */
        @Override
        public Collection<OperationMirror> operations() {
            // All operation that creates bean dependencies????
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isEmpty() {
            // what about just dependencies on extensions???
            return false;
        }
    }

    /** This class describes the relationship between two different beans. */
    // Do we support relationship to itself? I would think it was always an error?
    // Take two mirrors instead and let people override it??? ServiceDependencyMirror extends BRM
    public final class Relationship {

        /** The from bean of the relationship. */
        private final BeanSetup from;

        /** The to bean of the relationship. */
        private final BeanSetup to;

        /**
         * Creates a new mirror
         *
         * @param from
         *            the from part of the relationship
         * @param to
         *            the to part of the relationship
         */
        private Relationship(BeanSetup from, BeanSetup to) {
            this.from = requireNonNull(from);
            this.to = requireNonNull(to);
        }

        @SuppressWarnings("exports") // uses sandbox classes
        public DependenciesMirror dependencies() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Relationship o && (from == o.from && to == o.to);
        }

        /** {@return a mirror of the from bean of the relationship.} */
        public BeanMirror from() {
            return from.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return to.hashCode() ^ from.hashCode();
        }

        /** {@return whether or not the beans are in the same application.} */
        public boolean isInSameApplication() {
            return from.container.application == to.container.application;
        }

        /** {@return whether or not the beans are in the same container.} */
        public boolean isInSameContainer() {
            return from.container == to.container;
        }

        /** {@return whether or not the beans are in the same lifetime.} */
        public boolean isInSameLifetime() {
            return from.lifetime == to.lifetime;
        }

        public boolean isInSameRealm() {
            return from.author().equals(to.author());
        }

        /** {@return the reverse relationship.} */
        public Relationship reverse() {
            return new Relationship(to, from);
        }

        /** {@return a mirror of the to bean of the relationship.} */
        public BeanMirror to() {
            return to.mirror();
        }
    }
}

interface SelectableBeanMirror {

    // Man kan jo godt tage Assembly, Deployment osv. Giver bare et enkelt resultat
    // Operations, Bindings,
    Stream<OperationMirror> select(Class<? extends OperationMirror> operations);
}

interface SSandbox {

    // @SuppressWarnings({ "unchecked", "rawtypes" })
    default Optional<Object /* BeanFactoryOperationMirror */> factory() {
        // return (Optional) operations().stream().filter(m ->
        // BeanFactoryOperationMirror.class.isAssignableFrom(m.getClass())).findAny();
        // Kunne man forstille sig at en bean havde 2 constructors??
        // Som man valgte af paa runtime????
        throw new UnsupportedOperationException();
    }

    default Class<? extends Extension<?>> installedVia() {
        // The extension that performed the actual installation of the bean
        // Den burde ligge paa Component???
        // Nah
        return BaseExtension.class;
    }

    // No instances, Instantiable, ConstantInstance
    // Scope-> BuildConstant, RuntimeConstant, Prototype...
}

///**
//* Invoked by the runtime with the internal configuration of the bean to mirror.
//*
//* @param bean
//*            the internal configuration of the bean to mirror
//*/
//final void initialize(BeanSetup bean) {
//  if (this.bean != null) {
//      throw new IllegalStateException("This mirror has already been initialized.");
//  }
////  this.bean = bean;
//}
//
///**
//* {@return the internal configuration of the bean we are mirroring.}
//*
//* @throws IllegalStateException
//*             if {@link #initialize(BeanSetup)} has not been called previously.
//*/
//private BeanSetup bean {
//  BeanSetup b = bean;
//  if (b == null) {
//      throw new IllegalStateException(
//              "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
//  }
//  return b;
//}

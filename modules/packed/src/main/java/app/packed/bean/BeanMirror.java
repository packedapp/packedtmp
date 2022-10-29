package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.MirrorExtension;
import app.packed.container.User;
import app.packed.lifetime.ContainerLifetimeMirror;
import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationMirror;
import app.packed.operation.bindings.DependenciesMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.Mirror;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.StreamUtil;

/**
 * A mirror of a bean.
 * <p>
 * Instances of this class is typically obtained from calls to {@link ApplicationMirror} or {@link ContainerMirror}.
 */
@BindingHook(extension = MirrorExtension.class)
public class BeanMirror implements Mirror {

    /**
     * The internal configuration of the bean we are mirroring. Is initially null but populated via
     * {@link #initialize(BeanSetup)}.
     */
    @Nullable
    private BeanSetup bean;

    /**
     * Create a new bean mirror.
     */
    public BeanMirror() {}

    /** {@return the application where this bean is defined.} */
    public ApplicationMirror application() {
        return bean().container.application.mirror();
    }

    /** {@return the assembly where the bean is defined.} */
    public AssemblyMirror assembly() {
        return bean().container.assembly.mirror();
    }

    /**
     * {@return the internal configuration of the bean we are mirroring.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(BeanSetup)} has not been called previously.
     */
    private BeanSetup bean() {
        BeanSetup b = bean;
        if (b == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return b;
    }

    /**
     * Returns the type (class) of the bean.
     * <p>
     * Beans that do not have a proper class, for example, a {@link BeanKind#FUNCTIONAL functional} bean. Will have
     * {@code void.class} as their bean class.
     * 
     * @return the type (class) of the bean.
     */
    public Class<?> beanClass() {
        return bean().beanClass;
    }

    /** {@return the container the bean belongs to.} */
    public ContainerMirror container() {
        return bean().container.mirror();
    }

    /** {@return the dependencies this bean introduces.} */
    public DependenciesMirror dependencies() {
        return new BeanDependenciesMirror(bean());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof BeanMirror m && bean() == m.bean();
    }

    /**
     * If instances of this bean is created at runtime. This method will return the operation that creates the instance.
     * 
     * @return operation that creates instances of the bean. Or empty if instances are never created
     */
    public Optional<OperationMirror> factoryOperation() {
        BeanSetup bean = bean();
        if (bean.beanKind.hasInstances() && bean.sourceKind != BeanSourceKind.INSTANCE) {
            return Optional.of(bean.operations.get(0).mirror());
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return bean().hashCode();
    }

    /**
     * Invoked by the runtime with the internal configuration of the bean to mirror.
     * 
     * @param bean
     *            the internal configuration of the bean to mirror
     */
    final void initialize(BeanSetup bean) {
        if (this.bean != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.bean = bean;
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
        return bean().lifetime.mirror();
    }

    public Collection<LifetimeMirror> managesLifetimes() {
        // Find LifetimeOperations->Unique on Lifetime
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the name of this bean.
     * <p>
     * If no name was explicitly set when the bean was configured. Packed will automatically assign an unique name to it.
     *
     * @return the name of this bean
     */
    public String name() {
        return bean().name;
    }

    /** {@return a stream of all of the operations declared by the bean.} */
    public Stream<OperationMirror> operations() {
        return bean().operations.stream().map(OperationSetup::mirror);
    }

    /**
     * Returns a stream of all of the operations declared by the bean with the specified mirror type.
     * 
     * @param <T>
     * @param operationType
     *            the type of operations to include
     * @return a collection of all of the operations declared by the bean of the specified type.
     */
    public <T extends OperationMirror> Stream<T> operations(Class<T> operationType) {
        requireNonNull(operationType, "operationType is null");
        return StreamUtil.filterAssignable(operationType, operations());
    }

    /**
     * Returns any extension the bean's driver is part of. All drivers are either part of an extension. Or is a build in
     * drive
     * <p>
     * Another thing is extension member, which is slightly different.
     * 
     * @return any extension the bean's driver is part of
     */
    Class<? extends Extension<?>> operator() { // registrant
        return bean().installedBy.extensionType;
    }

    /** {@return the owner of the bean.} */
    public User owner() {
        return bean().realm.realm();
    }

    public NamespacePath path() {
        return bean().path();
    }

    /**
     * @param to
     *            the bean to return a relationship mirror to
     * @return a bean relationship mirror to the
     */
    public Relationship relationshipTo(BeanMirror to) {
        requireNonNull(to, "to is null");
        return new Relationship(bean(), to.bean());
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
                    if (b.boundBy().isExtension()) {
                        if (b.boundBy() != bean.realm.realm()) {
                            set.add((b.boundBy().extension()));
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
            return from.realm.realm().equals(to.realm.realm());
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
        return BeanExtension.class;
    }

    // No instances, Instantiable, ConstantInstance
    // Scope-> BuildConstant, RuntimeConstant, Prototype...
}

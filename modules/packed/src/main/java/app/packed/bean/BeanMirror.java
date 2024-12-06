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
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanBuildLocal.Accessor;
import app.packed.bean.lifecycle.BeanLifecycleMirror;
import app.packed.bean.scanning.BeanTrigger.OnExtensionServiceInteritedBeanTrigger;
import app.packed.binding.Key;
import app.packed.build.action.BuildActionMirror;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.container.ContainerMirror;
import app.packed.context.Context;
import app.packed.context.ContextMirror;
import app.packed.context.ContextScopeMirror;
import app.packed.context.ContextualizedElementMirror;
import app.packed.extension.Extension;
import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationMirror;
import app.packed.service.mirror.ServiceBindingMirror;
import app.packed.service.mirrorold.ServiceProviderIsThisUsefulMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.extension.BaseExtensionMirrorBeanIntrospector;
import internal.app.packed.lifecycle.PackedBeanLifecycleMirror;
import internal.app.packed.operation.OperationSetup;
import sandbox.operation.mirror.DependenciesMirror;

/**
 * A mirror of a bean.
 * <p>
 * An instance of BeanMirror (or a subclass hereof) can be injected at runtime simply by declaring a dependency on it.
 */
@OnExtensionServiceInteritedBeanTrigger(introspector = BaseExtensionMirrorBeanIntrospector.class)
public non-sealed class BeanMirror implements Accessor, ComponentMirror, ContextualizedElementMirror, ContextScopeMirror, ServiceProviderIsThisUsefulMirror {

    /** The handle of the bean we are mirroring. */
    private final BeanHandle<?> handle;

    /**
     * Create a new bean mirror.
     *
     * @throws IllegalStateException
     *             if attempting to explicitly construct a bean mirror instance
     */
    public BeanMirror(BeanHandle<?> handle) {
        this.handle = requireNonNull(handle);
    }

    /** {@return the application the bean is a part of.} */
    public ApplicationMirror application() {
        return handle.bean.container.application.mirror();
    }

    /**
     * {@return the assembly where the bean's container is defined.}
     * <p>
     * In case of beans owned by an extension. The assembly representing the container in which the bean is located is
     * returned.
     */
    public AssemblyMirror assembly() {
        return handle.bean.container.assembly.mirror();
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
        return handle.bean.beanClass;
    }

    /** {@return the bean kind} */
    public final BeanKind beanKind() {
        return handle.bean.beanKind;
    }

    /** {@return the bean source kind} */
    public final BeanSourceKind beanSourceKind() {
        return handle.bean.beanSourceKind;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return handle.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public final Set<String> componentTags() {
        return handle.componentTags();
    }

    /** {@return the container the bean belongs to.} */
    public ContainerMirror container() {
        return handle.bean.container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    // Is injectable into the bean factory. not all methods
    public final Map<Class<? extends Context<?>>, ContextMirror> contexts() {
        return ContextSetup.allMirrorsFor(handle.bean);
    }

    /** {@return the dependencies this bean introduces.} */
    @SuppressWarnings("exports") // uses sandbox classes
    public DependenciesMirror dependencies() {
        return new BeanDependenciesMirror(handle.bean);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof BeanMirror m && handle.bean == m.handle.bean;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return handle.bean.hashCode();
    }

    BuildActionMirror installationAction() {
        throw new UnsupportedOperationException();
    }

    /** {@return the extension that installed the bean, typically BaseExtension} */
    public final Class<? extends Extension<?>> installedByExtension() {
        return handle.bean.installedBy.extensionType;
    }

    /** {@return a mirror detailing the lifecycle of the bean} */
    public final BeanLifecycleMirror lifecycle() {
        return new PackedBeanLifecycleMirror(handle.bean);
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
        return handle.bean.lifetime.mirror();
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
        return handle.bean.name();
    }

    /** {@return a stream of all of the operations declared by the bean.} */
    public final OperationMirror.OfStream<OperationMirror> operations() {
        return OperationMirror.OfStream.of(handle.bean.operations.stream().map(OperationSetup::mirror));
    }

    public Map<Key<?>, Collection<ServiceBindingMirror>> overriddenServices() {
        throw new UnsupportedOperationException();
    }

    /** {@return the owner of the bean.} */
    public final ComponentRealm owner() {
        return handle.owner();
    }

//    /** {@return any proxy the bean may have.} */
//    public final Optional<BeanProxyMirror> proxy() {
//        return Optional.empty();
//    }

    /**
     * @param to
     *            the bean to return a relationship mirror to
     * @return a bean relationship mirror to the
     */
    public final Relationship relationshipTo(BeanMirror to) {
        requireNonNull(to, "to is null");
        BeanSetup other = to.handle.bean;
        if (handle.bean.container.application.deployment != other.container.application.deployment) {
            throw new IllegalArgumentException("The specified bean is not part of the same deployment as this bean");
        }
        return new Relationship(handle.bean, other);
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
                        if (b.boundBy != bean.owner()) {
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
            return from.owner().equals(to.owner());
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

    // No instances, Instantiable, ConstantInstance
    // Scope-> BuildConstant, RuntimeConstant, Prototype...
}

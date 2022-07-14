package app.packed.container;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.ComponentMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionMirrorHelper;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.Mirror;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.StreamUtil;

/**
 * A mirror of a container.
 * <p>
 * Instances of this class is typically via {@link ApplicationMirror}.
 */
public non-sealed class ContainerMirror implements ComponentMirror , Mirror {

    /**
     * The internal configuration of the container we are mirroring. Is initially null but populated via
     * {@link #initialize(ContainerSetup)}.
     */
    @Nullable
    private ContainerSetup container;

    /**
     * Create a new container mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public ContainerMirror() {}

    /**
     * {@return the internal configuration of the container we are mirroring.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(ContainerSetup)} has not been called previously.
     */
    private ContainerSetup container() {
        ContainerSetup c = container;
        if (c == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof ContainerMirror m && container() == m.container();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return container().hashCode();
    }

    /**
     * Invoked by the runtime with the internal configuration of the container to mirror.
     * 
     * @param bean
     *            the internal configuration of the container to mirror
     */
    final void initialize(ContainerSetup container) {
        if (this.container != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.container = container;
    }

    /** {@return a {@link Collection} view of all the beans defined in the container.} */
    public Collection<BeanMirror> beans() {
        // return CollectionUtil.unmodifiableView(children.values(), c -> c.mirror());
        throw new UnsupportedOperationException();
        // we need a filter on the view...
        // size, isEmpty, is going to get a bit slower.
    }

    /** {@return a {@link Set} view of every extension that have been used in the container.} */
    // return Map<Class<Ext>, Mirror> instead???
    // Altsaa hvad vil bruge metoden til???
    // Kan ikke lige umiddelbart se nogle use cases
    // Maaske bare fjerne den
    public Set<ExtensionMirror<?>> extensions() {
        HashSet<ExtensionMirror<?>> result = new HashSet<>();
        for (ExtensionSetup extension : container().extensions.values()) {
            result.add(ExtensionMirrorHelper.newMirrorOfUnknownType(extension));
        }
        return Set.copyOf(result);
    }

    /** {@return an unmodifiable view of all of the children of this component.} */
    /* Sequenced */ 
    public Collection<ContainerMirror> children() {
        return CollectionUtil.unmodifiableView(container().containerChildren, c -> c.mirror());
    }

    /** {@return the parent container of this container. Or empty if the root container.} */
    public Optional<ContainerMirror> parent() {
        ContainerSetup p = container().parent;
        return p == null ? Optional.empty() : Optional.of(p.mirror());
    }

    /** {@return a {@link Set} view of every extension type that have been used in the container.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container().extensionTypes();
    }
    /**
     * <p>
     * If you know for certain that extension is used in the container you can use {@link #useExtension(Class)} instead.
     * 
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the mirror type
     * @return a mirror of the specified type, or empty if the extension the mirror represents is not used in the container
     */
    @SuppressWarnings("unchecked")
    public <T extends ExtensionMirror<?>> Optional<T> findExtension(Class<T> mirrorType) {
        ClassUtil.checkProperSubclass(ExtensionMirror.class, mirrorType, "mirrorType");
        return (Optional<T>) Optional.ofNullable(ExtensionMirrorHelper.newMirrorOrNull(container(), mirrorType));
    }
    /**
     * Returns whether or not an extension of the specified type is in use by the container.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if the container uses an extension of the specified type, otherwise {@code false}
     * @see ContainerConfiguration#isExtensionUsed(Class)
     */
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return container().isExtensionUsed(extensionType);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ContainerMirror (" + path() + ")";
    }
    
    /**
     * Returns an mirror of the specified type if the container is using the extension the mirror is a part of. Or throws
     * {@link NoSuchElementException} if the container does not use the specified extension type.
     * 
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the type of mirror to return
     * @return a mirror of the specified type
     * @see ContainerConfiguration#use(Class)
     * @see ApplicationMirror#useExtension(Class)
     * @see #findExtension(Class)
     * @throws NoSuchElementException
     *             if the mirror's extension is not in use by the container
     */
    public <T extends ExtensionMirror<?>> T useExtension(Class<T> extensionMirrorType) {
        return findExtension(extensionMirrorType).orElseThrow();
    }
    
    /** {@inheritDoc} */
    @Override
    public Stream<OperationMirror> operations() {
        return StreamUtil.filterAssignable(BeanSetup.class, container().children.values().stream()).flatMap(b -> b.mirror().operations());
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror application() {
        return container().application.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyMirror assembly() {
        return container().userRealm.mirror();
    }

    /** {@inheritDoc} */
    public final Stream<ComponentMirror> stream() {
        return container().stream().map(c -> c.mirror());
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return container().depth;
    }

    /** {@inheritDoc} */
    @Override
    public LifetimeMirror lifetime() {
        return container().lifetime.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return container().name;
    }


    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return container().path();
    }
}

package app.packed.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.lifetime.ContainerLifetimeMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.Mirror;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.typevariable.TypeVariableExtractor;

/**
 * A mirror of a single container.
 * <p>
 * Instances of this class is typically via {@link ApplicationMirror}.
 */
public class ContainerMirror implements Mirror {

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

    /** {@return the application this container is a part of.} */
    public ApplicationMirror application() {
        return container().application.mirror();
    }

    /** {@return the assembly where the container is defined.} */
    public AssemblyMirror assembly() {
        return container().assembly.mirror();
    }

    /** {@return a {@link Collection} view of all the beans defined in the container.} */
    public Collection<BeanMirror> beans() {
        // not technically a view but will do for now
        ArrayList<BeanMirror> beans = new ArrayList<>();
        for (Object s : container.children.values()) {
            if (s instanceof BeanSetup b) {
                beans.add(b.mirror());
            }
        }
        return List.copyOf(beans);
        // return CollectionUtil.unmodifiableView(children.values(), c -> c.mirror());
        // we need a filter on the view...
        // size, isEmpty, is going to get a bit slower.
    }

    /** {@return an unmodifiable view of all of the children of this component.} */
    /* Sequenced */
    public Collection<ContainerMirror> children() {
        // does not work because container().containerChildren may be null
        return CollectionUtil.unmodifiableView(container().containerChildren, c -> c.mirror());
    }

    public final Stream<ContainerMirror> descendents(boolean includeThis) {
        throw new UnsupportedOperationException();
    }

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

    /** {@return a {@link Set} view of every extension that have been used in the container.} */
    // return Map<Class<Ext>, Mirror> instead???
    // Altsaa hvad vil bruge metoden til???
    // Kan ikke lige umiddelbart se nogle use cases
    // Maaske bare fjerne den
    public Set<ExtensionMirror<?>> extensions() {
        HashSet<ExtensionMirror<?>> result = new HashSet<>();
        for (ExtensionSetup extension : container().extensions.values()) {
            result.add(newMirrorOfUnknownType(extension));
        }
        return Set.copyOf(result);
    }

    public boolean isRoot() {
        return container().treeParent == null;
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
        return (Optional<T>) Optional.ofNullable(newMirrorOrNull(container(), mirrorType));
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

    /** {@return the containers's lifetime.} */
    public ContainerLifetimeMirror lifetime() {
        return container().lifetime().mirror();
    }

    /**
     * Returns the name of this container.
     * <p>
     * If no name was explicitly set when the container was configured. Packed will automatically assign an unique name to
     * it.
     *
     * @return the name of this container
     */
    public String name() {
        return container().name;
    }

    /** {@return the parent container of this container. Or empty if the root container.} */
    public Optional<ContainerMirror> parent() {
        ContainerSetup p = container().treeParent;
        return p == null ? Optional.empty() : Optional.of(p.mirror());
    }

    public NamespacePath path() {
        return container().path();
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
    
    /** A ExtensionMirror class to Extension class mapping. */
    private final static ClassValue<Class<? extends Extension<?>>> EXTENSION_TYPES = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor TYPE_LITERAL_EP_EXTRACTOR = TypeVariableExtractor.of(ExtensionMirror.class);

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            // Extract the type of extension from ExtensionMirror<E>
            Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) TYPE_LITERAL_EP_EXTRACTOR.extractProperSubClassOf(type,
                    Extension.class, InternalExtensionException::new);

            // Check that the mirror is in the same module as the extension itself
            if (extensionClass.getModule() != type.getModule()) {
                throw new InternalExtensionException("The extension mirror " + type + " must be a part of the same module (" + extensionClass.getModule()
                        + ") as " + extensionClass + ", but was part of '" + type.getModule() + "'");
            }

            return ExtensionDescriptor.of(extensionClass).type(); // Check that the extension is valid
        }
    };
    
    /** {@return a mirror for the extension. An extension might specialize by overriding {@code Extension#mirror()}} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static ExtensionMirror<?> newMirror(ExtensionSetup extension, Class<? extends ExtensionMirror<?>> expectedMirrorClass) {
        ExtensionMirror<?> mirror = extension.instance().newExtensionMirror();

        // Cannot return a null mirror
        if (mirror == null) {
            throw new InternalExtensionException(
                    "Extension " + extension.model.fullName() + " returned null from " + extension.model.name() + ".newExtensionMirror()");
        }

        // If we expect a mirror of a particular type, check it
        if (expectedMirrorClass != null) {
            // Fail if the type of mirror returned by the extension does not match the specified mirror type
            if (!expectedMirrorClass.isInstance(mirror)) {
                throw new InternalExtensionException(extension.extensionType.getSimpleName() + ".newExtensionMirror() was expected to return an instance of "
                        + expectedMirrorClass + ", but returned an instance of " + mirror.getClass());
            }
        } else if (mirror.getClass() != ExtensionMirror.class) {
            // Extensions are are allowed to return ExtensionMirror from newExtensionMirror in which case we have no additional
            // checks

            // If expectedMirrorClass == null we don't know what type of mirror to expect. Other than it must be parameterized with
            // the right extension

            // Must return a mirror for the same extension
            Class<? extends Extension<?>> mirrorExtensionType = EXTENSION_TYPES.get(mirror.getClass());
            if (mirrorExtensionType != extension.extensionType) {
                throw new InternalExtensionException(
                        "Extension " + extension.model.fullName() + " returned a mirror for another extension, other extension type: " + mirrorExtensionType);
            }
        }

        mirror.initialize(new ExtensionNavigatorImpl(extension, extension.extensionType));
        return mirror;
    }

    /**
     * Create a new mirror for extension
     * 
     * @param extension
     *            the extension
     * @return the new mirror
     */
    static ExtensionMirror<?> newMirrorOfUnknownType(ExtensionSetup extension) {
        return newMirror(extension, null);
    }

    /**
     * Creates a new mirror if an. Otherwise returns {@code null}
     * 
     * @param container
     *            the container to test for presence extension may be present
     * @param mirrorClass
     *            the type of mirror to return
     * @return a mirror of the specified type or null if no extension of the matching type was used in the container
     */
    @Nullable
    static ExtensionMirror<?> newMirrorOrNull(ContainerSetup container, Class<? extends ExtensionMirror<?>> mirrorClass) {
        // First find what extension the mirror belongs to by extracting <E> from ExtensionMirror<E extends Extension>
        Class<? extends Extension<?>> extensionClass = EXTENSION_TYPES.get(mirrorClass);

        // See if the container uses the extension.
        ExtensionSetup extension = container.extensions.get(extensionClass);

        return extension == null ? null : newMirror(extension, mirrorClass);
    }
}
//// Taken from ComponentMirror
// Now that we have parents...
// add Optional<Component> tryResolve(CharSequence path);
// Syntes ikke vi skal have baade tryResolve or resolve...
// ComponentMirror resolve(CharSequence path);

///**
// * Returns a stream consisting of this component and all of its descendants in any order.
// *
// * @param options
// *            specifying the order and contents of the stream
// * 
// * @return a component stream consisting of this component and all of its descendants in any order
// */
//ComponentMirrorStream stream(ComponentMirrorStream.Option... options);

///**
// * 
// * 
// * <p>
// * This operation does not allocate any objects internally.
// * 
// * @implNote Implementations of this method should never generate object (which is a bit difficult
// * @param action
// *            oops
// */
//// We want to take some options I think. But not as a options
//// Well it is more or less the same options....
//// Tror vi laver options om til en klasse. Og saa har to metoder.
//// Og dropper varargs..
//default void traverse(Consumer<? super ComponentMirror> action) {
//    stream(Option.maxDepth(1)).forEach(action);
//}

//// The returned component is always a system component
//default Component viewAs(Object options) {
//    // F.eks. tage et system. Og saa sige vi kun vil
//    // se paa den aktuelle container
//
//    // Ideen er lidt at vi kan taege en component
//    // Og f.eks. lave den om til en rod...
//    // IDK. F.eks. hvis jeg har guests app.
//    // Saa vil jeg gerne kunne sige til brugere...
//    // Her er en clean Guest... Og du kan ikke se hvad
//    // der sker internt...
//    throw new UnsupportedOperationException();
//}

//default Optional<ComponentMirror> tryResolve(CharSequence path) {
//    throw new UnsupportedOperationException();
//}

// Taenker den er immutable...

// Problemet er at vi vil have en snapshot...
// Tager vi snapshot af attributer??? Nej det goer vi altsaa ikke...
// Rimlig meget overhead

// Tror topol

// Vi skal ikke have noget live...
// Maaske af det derfor

// Special cases

// Not In same system
// -> distance = -1, inSameContainer=false, isStronglyBound=false, isWeaklyBound=false

// Same component
// -> distance = 0, inSameContainer=true, isStrongBound=?, isWeaklyBound=false

// Teanker vi flytter den et andet sted end attributer...

// Altsaa den walk man kan lave via Iterable... ville det vaere rart at kunne beskrive relationsship
// for den.. Altsaa cr[4] er foraeldre til cr[3] og saa

// A component in a runtime system is not the same as in the build time system.

// I would say the restartable image of one system is not in the same system as
// the same restartable image when we have restarted.

// https://en.wikipedia.org/wiki/Path_(graph_theory)
// ComponentConnection

// A ComponentRelation is directed
// Can we have attributes??
// if (rel.inSameContainer())

// En relation er jo mere end topology... Syntes

// walkFromSource(); Syntes maaske ikke den skal extende Iteralble...
// Maaske fromSourceIterator
// https://en.wikipedia.org/wiki/Tree_structure
// description()? -> Same, parent, child, descendend, ancestor,
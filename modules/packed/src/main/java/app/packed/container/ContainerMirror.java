package app.packed.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.NamespacePath;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanHook.VariableTypeHook;
import app.packed.context.ContextualizedElement;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.InternalExtensionException;
import app.packed.extension.MirrorExtension;
import app.packed.framework.Nullable;
import app.packed.lifetime.ContainerLifetimeMirror;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.Mirror;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.typevariable.TypeVariableExtractor;

/**
 * A mirror of a container.
 * <p>
 * At build-time you typically obtain a ContainerMirror by calling {@link ApplicationMirror#}
 * 
 * <p>
 * At runtime you can have a ContainerMirror injected 
 */
@VariableTypeHook(extension = MirrorExtension.class)
public non-sealed class ContainerMirror implements ContextualizedElement , Mirror {

    /** Extract the (extension class) type variable from ExtensionMirror. */
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

    /** A MethodHandle for invoking {@link ExtensionMirror#initialize(ExtensionSetup)}. */
    private static final MethodHandle MH_EXTENSION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ExtensionMirror.class,
            "initialize", void.class, ExtensionSetup.class);

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_EXTENSION_MIRROR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class, "newExtensionMirror",
            ExtensionMirror.class);

    /**
     * The internal configuration of the container we are mirroring. Is initially null but populated via
     * {@link #initialize(ContainerSetup)}.
     */
    @Nullable
    private ContainerSetup container;

    /** Create a new container mirror. */
    public ContainerMirror() {}

    /** {@return the application this container is a part of.} */
    public ApplicationMirror application() {
        return container().application.mirror();
    }

    /** {@return the assembly wherein this container was defined.} */
    public AssemblyMirror assembly() {
        return container().assembly.mirror();
    }

    /** {@return a {@link Collection} view of all the beans defined in the container.} */
    // returning stream vs collection... I took a look at the methods in Collection.
    // And size + isEmpty is the only interesting ones
    // Arghhh den er sgu rar for Iterable...
    // https://cr.openjdk.java.net/~smarks/reviews/8148917/IterableOnce0.html
    public Stream<BeanMirror> beans() {
        // not technically a view but will do for now
        ArrayList<BeanMirror> beans = new ArrayList<>();
        for (var b = container().beanFirst; b != null; b = b.nextBean) {
            beans.add(b.mirror());
        }
        return List.copyOf(beans).stream();
        // return CollectionUtil.unmodifiableView(children.values(), c -> c.mirror());
        // we need a filter on the view...
        // size, isEmpty, is going to get a bit slower.
    }

    /** {@return an unmodifiable view of all of the children of this component.} */
    public Stream<ContainerMirror> children() {
        // childIterable?
        // does not work because container().containerChildren may be null
        throw new UnsupportedOperationException();
        // return CollectionUtil.unmodifiableView(container().containerChildren, c -> c.mirror());
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

    public Stream<ContainerMirror> descendents(boolean includeThis) {
        // Maaske have en TreeSelector
        // Der er 3 interessant ting taenker jeg.
        // direct children
        // direct ancestors
        // direct ancestors + this
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof ContainerMirror m && container() == m.container();
    }

    /** {@return a {@link Set} view of every extension type that have been used in the container.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container().extensionTypes();
    }

    /**
     * <p>
     * If you know for certain that extension is used in the container you can use {@link #use(Class)} instead.
     * 
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the mirror type
     * @return a mirror of the specified type, or empty if the extension the mirror represents is not used in the container
     */
    public <T extends ExtensionMirror<?>> Optional<T> findExtension(Class<T> mirrorType) {
        ClassUtil.checkProperSubclass(ExtensionMirror.class, mirrorType, "mirrorType");
        return Optional.ofNullable(newMirrorOrNull(container(), mirrorType));
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

    /** {@return whether or not the container is the root container in the application.} */
    public boolean isApplicationRoot() {
        return container().treeParent == null;
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
    // Do we need isApplicationRoot, isLifetimeRoot
    public ContainerLifetimeMirror lifetime() {
        return container().lifetime.mirror();
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

    /** {@return the parent container of this container. Or empty if the root container in an application.} */
    public Optional<ContainerMirror> parent() {
        ContainerSetup p = container().treeParent;
        return p == null ? Optional.empty() : Optional.of(p.mirror());
    }

    /** {@return the path of the container.} */
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
     * @see ApplicationMirror#use(Class)
     * @see #findExtension(Class)
     * @throws NoSuchElementException
     *             if the mirror's extension is not in use by the container
     */
    public <T extends ExtensionMirror<?>> T use(Class<T> extensionMirrorType) {
        Optional<T> op = findExtension(extensionMirrorType);
        if (op.isEmpty()) {
            throw new NoSuchElementException(extensionMirrorType + " is not present in this container");
        }
        return op.get();
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
    static <T extends ExtensionMirror<?>> T newMirrorOrNull(ContainerSetup container, Class<T> mirrorClass) {
        // First find what extension the mirror belongs to by extracting <E> from ExtensionMirror<E extends Extension>
        Class<? extends Extension<?>> extensionClass = EXTENSION_TYPES.get(mirrorClass);

        // See if the container uses the extension.
        ExtensionSetup extension = container.extensions.get(extensionClass);

        if (extension == null) {
            return null;
        }

        Extension<?> instance = extension.instance();

        ExtensionMirror<?> mirror;
        try {
            mirror = (ExtensionMirror<?>) MH_NEW_EXTENSION_MIRROR.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }

        // Cannot return a null mirror
        if (mirror == null) {
            throw new InternalExtensionException(
                    "Extension " + extension.model.fullName() + " returned null from " + extension.model.name() + ".newExtensionMirror()");
        }

        // If we expect a mirror of a particular type, check it
        if (mirrorClass != null) {
            // Fail if the type of mirror returned by the extension does not match the specified mirror type
            if (!mirrorClass.isInstance(mirror)) {
                throw new InternalExtensionException(extension.extensionType.getSimpleName() + ".newExtensionMirror() was expected to return an instance of "
                        + mirrorClass + ", but returned an instance of " + mirror.getClass());
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

        try {
            MH_EXTENSION_MIRROR_INITIALIZE.invokeExact(mirror, extension);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return mirrorClass.cast(mirror);
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

///** {@return a {@link Set} view of every extension that have been used in the container.} */
//// return Map<Class<Ext>, Mirror> instead???
//// Altsaa hvad vil bruge metoden til???
//// Kan ikke lige umiddelbart se nogle use cases
//// Maaske bare fjerne den
//public Set<ExtensionDescriptor> extensions() {
//    HashSet<ExtensionDescriptor> result = new HashSet<>();
//    for (ExtensionSetup extension : container().extensions.values()) {
//        result.add(ExtensionDescriptor.of(extension.extensionType));
//    }
//    return Set.copyOf(result);
//}

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
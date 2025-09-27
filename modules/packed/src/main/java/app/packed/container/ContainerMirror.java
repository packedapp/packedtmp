package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanMirror;
import app.packed.bean.scanning.BeanTrigger.OnContextServiceInheritableVariable;
import app.packed.build.hook.BuildHookMirror;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.lifetime.CompositeLifetimeMirror;
import app.packed.namespace.NamespaceMirror;
import app.packed.operation.OperationMirror;
import app.packed.util.Nullable;
import app.packed.util.TreeView;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.MirrorImplementationBeanIntrospector;
import internal.app.packed.extension.ExtensionClassModel;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.util.PackedTreeView;
import internal.app.packed.util.types.ClassUtil;
import internal.app.packed.util.types.TypeVariableExtractor;

/**
 * A mirror of a container.
 * <p>
 * At build-time you typically obtain a ContainerMirror by calling {@link ApplicationMirror#}
 *
 * <p>
 * At runtime you can have a ContainerMirror injected
 */
@OnContextServiceInheritableVariable(introspector = MirrorImplementationBeanIntrospector.class)
public non-sealed class ContainerMirror implements ComponentMirror, ContainerBuildLocal.Accessor {

    /** Extract the (extension class) type variable from ExtensionMirror. */
    private final static ClassValue<Class<? extends Extension<?>>> EXTENSION_TYPES = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(ExtensionMirror.class);

        /** {@inheritDoc} */
        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            return ExtensionClassModel.extractE(EXTRACTOR, type);
        }
    };

    /** The container we are mirroring. */
    private final ContainerHandle<?> handle;

    /**
     * Create a new container mirror.
     *
     * @param handle
     *            the container's handle
     */
    public ContainerMirror(ContainerHandle<?> handle) {
        this.handle = requireNonNull(handle);
    }

    /** {@return a stream containing all beans defined by the container including beans that are declared by extensions.} */
    public final Stream<BeanMirror> allBeans() {
        return handle.container.beans.stream().map(b -> b.mirror());
    }

    /**
     * {@return a stream of all operations defined in the application}
     * <p>
     * Unlike {@link #operations()} the returned stream includes operations on beans owned by extensions.
     */
    public final OperationMirror.OfStream<OperationMirror> allOperations() {
        return OperationMirror.OfStream.of(allBeans().flatMap(BeanMirror::operations));
    }

    /** {@return the application this container is a part of.} */
    public ApplicationMirror application() {
        return handle.container.application.mirror();
    }

    /** {@return a node representing this container in a tree containing all containers in the application.} */
    public TreeView.Node<ContainerMirror> applicationNode() {
        return new PackedTreeView<>(handle.container.application.container(), null, c -> c.mirror()).toNode(handle.container);
    }

    /** {@return the assembly wherein this container was defined.} */
    public AssemblyMirror assembly() {
        return handle.container.assembly.mirror();
    }

    /**
     * {@return a stream of all beans that are declared by the application.}
     * <p>
     * Notice: The returned stream does not include beans that are declared by extensions, use {@link #allBeans()} if you
     * need to include those.
     */
    public final Stream<BeanMirror> beans() {
        return allBeans().filter(m -> m.owner() == ComponentRealm.application());
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

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof ContainerMirror m && handle.container == m.handle.container;
    }

//    /** {@return the deployment this container is a part of.} */
//    public DeploymentMirror deployment() {
//        return container.application.deployment.mirror();
//    }

    /** {@return a {@link Set} view of all extensions that are used in the container.} */
    public final Set<Class<? extends Extension<?>>> extensionTypes() {
        return handle.container.extensionTypes();
    }

    /**
     * <p>
     * If you know for certain that extension is used in the container you can use {@link #use(Class)} instead.
     *
     * @param <T>
     *            the type of mirror
     * @param mirrorType
     *            the mirror type
     * @return a mirror of the specified type, or empty if the extension the mirror represents is not used in the container
     */
    public final <T extends ExtensionMirror<?>> Optional<T> findExtension(Class<T> mirrorType) {
        ClassUtil.checkProperSubclass(ExtensionMirror.class, mirrorType, "mirrorType");
        return Optional.ofNullable(newMirrorOrNull(handle.container, mirrorType));
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return handle.container.hashCode();
    }

    /** {@return the extension that installed the container} */
    public final Class<? extends Extension<?>> installedByExtension() {
        return BaseExtension.class; // TODO fix
    }

    /**
     * Returns whether or not an extension of the specified type is in use by the container.
     *
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if the container uses an extension of the specified type, otherwise {@code false}
     * @see ContainerConfiguration#isExtensionUsed(Class)
     */
    public final boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return handle.container.isExtensionUsed(extensionType);
    }

    /** {@return the containers's lifetime.} */
    public final CompositeLifetimeMirror lifetime() {
        return handle.container.lifetime.mirror();
    }

    /**
     * Returns the name of this container.
     * <p>
     * If no name is explicitly set for the container. The framework will automatically assign an unique (among siblings)
     * name to it.
     *
     * @return the name of this container
     */
    public final String name() {
        return handle.container.name();
    }

    /** {@return all the namespaces this container operates within.} */
    public final Stream<NamespaceMirror<?>> namespaces() {
        throw new UnsupportedOperationException();
    }

    /** {@return a stream of all of the operations declared on beans in the container owned by the user} */
    public final OperationMirror.OfStream<OperationMirror> operations() {
        return OperationMirror.OfStream.of(beans().flatMap(BeanMirror::operations));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ContainerMirror (" + componentPath() + ")";
    }

    /** {@return the transformers that has been applied to this container.} */
    public Stream<BuildHookMirror> transformers() {
        throw new UnsupportedOperationException();
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

    public List<WireletMirror> wirelets() {
        // On runtime we would need to add runtime wirelets
        throw new UnsupportedOperationException();
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
        // Extract <E> from ExtensionMirror<E extends Extension>
        Class<? extends Extension<?>> extensionType = EXTENSION_TYPES.get(mirrorClass);

        ExtensionMirror<?> mirror = null;

        // See if the extension is in use.
        ExtensionSetup extension = container.extensions.get(extensionType);
        if (extension != null) {
            // Call Extension#newExtensionMirror
            mirror = extension.newExtensionMirror(mirrorClass);
            requireNonNull(mirror);
            // Should take an ExtensionHandle
        }

        return mirrorClass.cast(mirror);
    }
}

//public static Assembly verifiable(Assembly assembly, Consumer<? super ContainerMirror> verifier) {
//  return Assemblies.verify(assembly, ContainerMirror.class, verifier);
//}

// Hvis vi siger at et domain er hele appen. Hvad goere vi i C3. Er den tilgaengelig under et "fake" navn???
//

// Vi skal have den fordi namespace simpelthen bliver noedt til at definere den
// Vi har en main database der bruges i P og saa bruger vi den i C1, C2 bruger den under alias "NotMain", og definere
// sin egen main.
// C3 definere kun sig egen main
//
///**
// * A view of all all of this containers beans that are in the same lifetime as the container itself.
// * <p>
// * If you need to include beans that are also owned by extension's use {@link #allBeans()}.
// *
// * @see #allBeans()
// */
//public Stream<BeanMirror> beansInContainerLifetime() {
//    return handle.container.beans.stream().filter(b -> b.lifetime == handle.container.lifetime).map(b -> b.mirror());
//}

//
///** {@return a set of all boundaries to this container's parent. Or empty if family root.} */
//public EnumSet<ContainerBoundaryKind> bondariesToParent() {
//  ContainerSetup parent = handle.container.treeParent;
//  if (parent != null) {
//      ContainerSetup c = handle.container;
//
//      // Deployment has all
//      if (parent.application.deployment != c.application.deployment) {
//          return EnumSet.allOf(ContainerBoundaryKind.class);
//      }
//
//      ArrayList<ContainerBoundaryKind> l = new ArrayList<>();
//
//      if (parent.application == c.application) {
//          l.add(ContainerBoundaryKind.APPLICATION);
//      }
//      if (parent.lifetime == c.lifetime) {
//          l.add(ContainerBoundaryKind.LIFETIME);
//      }
//      if (parent.assembly == c.assembly) {
//          l.add(ContainerBoundaryKind.ASSEMBLY);
//      }
//
//      if (!l.isEmpty()) {
//          return EnumSet.copyOf(l);
//      }
//  }
//  return EnumSet.noneOf(ContainerBoundaryKind.class);
//
//}

//// MAYBE MAYBE, but need some use cases
///** {@return a node representing this container within an application.} */

// maybe nodeInApplication();
// nodeInAssembly();
//public TreeView.Node<ContainerMirror> applicationNode() {
//    throw new UnsupportedOperationException();
//}

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
//    for (ExtensionSetup extension : container.extensions.values()) {
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
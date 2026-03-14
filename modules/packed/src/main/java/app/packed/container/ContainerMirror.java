package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanTrigger.AutoServiceInheritable;
import app.packed.binding.Key;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.namespace.NamespaceMirror;
import app.packed.operation.OperationMirror;
import app.packed.util.TreeView;
import internal.app.packed.bean.introspection.IntrospectorOnAutoService;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.util.PackedTreeView;

/**
 * A mirror of a container.
 * <p>
 * At build-time you typically obtain a ContainerMirror by calling {@link ApplicationMirror#}
 *
 * <p>
 * At runtime you can have a ContainerMirror injected
 */
@AutoServiceInheritable(introspector = ContainerMirrorBeanIntrospector.class)
public non-sealed class ContainerMirror implements ComponentMirror, ContainerBuildLocal.Accessor {

    /** The container we are mirroring. */
    final ContainerHandle<?> handle;

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
    // Hmm, giver det kun mening at have den paa application????
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
        return new PackedTreeView<>(handle.container.application.rootContainer(), null, c -> c.mirror()).toNode(handle.container);
    }

    /** {@return the assembly wherein this container was defined.} */
    public final AssemblyMirror assembly() {
        return handle.container.assembly.mirror();
    }

    /**
     * {@return a stream of all beans that are declared by the application.}
     * <p>
     * Notice: The returned stream does not include beans that are declared by extensions, use {@link #allBeans()} if you
     * need to include those.
     */
    public final Stream<BeanMirror> beans() {
        return allBeans().filter(m -> m.owner() == ComponentRealm.userland());
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

    /** {@return a {@link Set} view of all extensions that are used in the container.} */
    public final Set<Class<? extends Extension<?>>> extensionTypes() {
        return handle.container.extensionTypes();
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

    /** {@return the namespace this container is a part of} */
    public final NamespaceMirror namespace() {
        return handle.container.namespace.mirror();
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

//    /** {@return the transformers that has been applied to this container.} */
//    public Stream<BuildHookMirror> transformers() {
//        throw new UnsupportedOperationException();
//    }

    public final List<WireletMirror> wirelets() {
        // On runtime we would need to add runtime wirelets
        throw new UnsupportedOperationException();
    }

}

final class ContainerMirrorBeanIntrospector extends BaseExtensionBeanIntrospector {

    @Override
    public void onExtensionService(Key<?> key, IntrospectorOnAutoService service) {
        service.binder().bindConstant(container().mirror());
    }
}

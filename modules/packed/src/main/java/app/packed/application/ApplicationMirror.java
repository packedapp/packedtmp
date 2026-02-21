package app.packed.application;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.stream.Stream;

import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanTrigger.AutoServiceInheritable;
import app.packed.binding.Key;
import app.packed.build.BuildGoal;
import app.packed.build.Mirror;
import app.packed.build.MirrorPrinter;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.namespace.NamespaceMirror;
import app.packed.namespace.OverviewMirror;
import app.packed.operation.OperationMirror;
import app.packed.service.ServiceContract;
import app.packed.util.TreeView;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.introspection.IntrospectorOnAutoService;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.PackedTreeView;

/**
 * A mirror of an application.
 * <p>
 * An application mirror instance is typically obtained by calling application mirror factory methods such as
 * {@link App#mirrorOf(Assembly, Wirelet...)}.
 * <p>
 * Instances of ApplicationMirror (or a subclass hereof) can be injected into any bean simply by declaring a dependency
 * on this class.
 * <p>
 * Like many other mirrors classes the type of application mirror being returned can be specialized. See
 * {@link BootstrapApp.Composer#specializeMirror(java.util.function.Supplier)} for details.
 */
@AutoServiceInheritable(introspector = ApplicationMirrorIntrospector.class)
public non-sealed class ApplicationMirror implements ComponentMirror, ApplicationLocal.Accessor {

    /** The application's handle. */
    final ApplicationHandle<?, ?> handle;

    /**
     * Create a new application mirror.
     *
     * @param handle
     *            the application's handle
     */
    public ApplicationMirror(ApplicationHandle<?, ?> handle) {
        this.handle = requireNonNull(handle);
    }

    /**
     * {@return a stream of all of the operations declared in the application}
     * <p>
     * Unlike {@link #beans()}, this returned stream includes beans that are owned by extensions.
     */
    public Stream<BeanMirror> allBeans() {
        return containers().stream().flatMap(ContainerMirror::allBeans);
    }

    /**
     * {@return a stream of all operations defined in the application}
     * <p>
     * Unlike {@link #operations()} the returned stream includes operations on beans owned by extensions.
     */
    public OperationMirror.OfStream<OperationMirror> allOperations() {
        return OperationMirror.OfStream.of(allBeans().flatMap(BeanMirror::operations));
    }

    /** {@return a tree representing all the assemblies used for creating this application} */
    public TreeView<AssemblyMirror> assemblies() {
        return new PackedTreeView<>(handle.application.container().assembly, null, c -> c.mirror());
    }

    /** {@return a mirror of the (root) assembly that defines the application} */
    public AssemblyMirror assembly() {
        return handle.application.container().assembly.mirror();
    }

    /** {@return a stream of all of the bean declared by the user in the application.} */
    public Stream<BeanMirror> beans() {
        return containers().stream().flatMap(ContainerMirror::beans);
    }
    // ApplicationMirror
    // All namespaces with root container
    // All namespaces in the whole application
    // All namespaces with a non-user owner

    /** {@return the build goal that was used when building the application} */
    public BuildGoal buildGoal() {
        return handle.application.deployment.goal;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return handle.application.componentPath();
    }

    /**
     * {@return an immutable set of tags that have been set on the application}
     *
     * @see ApplicationConfiguration#componentTag(String...)
     * @see ApplicationHandle#componentTag(String...)
     * @see ApplicationTemplate.Installer#componentTag(String...)
     * @see ApplicationTemplate.Configurator#componentTag(String...)
     **/
    @Override
    public Set<String> componentTags() {
        return handle.componentTags();
    }

    /** {@return a mirror of the root container in the application.} */
    public ContainerMirror container() {
        return handle.application.container().mirror();
    }

    /** {@return a container tree mirror representing all the containers defined within the application.} */
    public TreeView<ContainerMirror> containers() {
        return new PackedTreeView<>(handle.application.container(), null, c -> c.mirror());
    }
//
//    /** {@return a collection of all entry points the application may have.} */
//    public Stream<OperationMirror> entryPoints() {
//        return container().lifetime().entryPoints();
//    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof ApplicationMirror m && handle.application == m.handle.application;
    }

    /** {@return an unmodifiable {@link Set} view of every extension type that has been used in the application.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container().extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return handle.application.hashCode();
    }

    /**
     * Tests if a given extension is used by the application.
     *
     * @param extensionClass
     *            the type of extension
     * @return true if the extension is used, otherwise false
     */
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionClass) {
        return container().isExtensionUsed(extensionClass);
    }

    /**
     * Returns the name of the application.
     * <p>
     * The name of an application is always identical to the name of the root container.
     *
     * @return the name of the application
     * @see Wirelet#named(String)
     */
    public String name() {
        return handle.application.container().name();
    }

    /** {@return a mirror of the root namespace in the application.} */
    public NamespaceMirror namespace() {
        return handle.application.container().namespace.mirror();
    }

    /** {@return a namespace tree mirror representing all the namespaces defined within the application.} */
    public TreeView<NamespaceMirror> namespaces() {
        return new PackedTreeView<>(handle.application.container().namespace, null, c -> c.mirror());
    }

    /** {@return a stream of all of the operations on beans owned by the user in the application.} */
    // I think non-synthetic should also be filtered
    public OperationMirror.OfStream<OperationMirror> operations() {
        return OperationMirror.OfStream.of(beans().flatMap(BeanMirror::operations));
    }

    public <O extends OverviewMirror<?>> O overview(Class<O> type) {
        throw new UnsupportedOperationException();
    }

    public void print() {
        // Maybe return ApplicationPrinter???
        // to(PrintStream ps);
        // asJSON();
        // verbose();
        print0(handle.application.container());
    }

    public final void print(@SuppressWarnings("unchecked") Class<? extends Mirror>... mirrorTypes) {

    }

    private void print0(ContainerSetup cs) {
        for (var e = cs.treeFirstChild; e != null; e = e.treeNextSibling) {
            print0(e);
        }
        for (BeanSetup b : cs.beans) {
            StringBuilder sb = new StringBuilder();
            sb.append(b.componentPath()).append("");
            sb.append(" [").append(b.bean.beanClass.getName()).append("], owner = " + b.owner());
            sb.append("\n");
            for (OperationSetup os : b.operations) {
                // sb.append(" ".repeat(b.path().depth()));
                sb.append("    o ");
                sb.append(os.mirror());
                sb.append("\n");
            }
            IO.print(sb.toString());
        }
    }

    /**
     *
     */
    public MirrorPrinter printer() {
        throw new UnsupportedOperationException();
    }

    /** {@return the service contract of this application.} */
    public ServiceContract serviceContract() {
        return handle.application.container().servicesMain().newContract();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Application:" + name();
    }
}

final class ApplicationMirrorIntrospector extends BaseExtensionBeanIntrospector {

    @Override
    public void onExtensionService(Key<?> key, IntrospectorOnAutoService service) {
        service.binder().bindConstant(service.bean().container.application.mirror());
    }
}

//
//// All mirrors "owned" by the user
//public Stream<ComponentMirror> components() {
//  throw new UnsupportedOperationException();
//}
//
/// ** {@return a mirror of the root assembly that defines the application.} */ IDK if we want this or only assemblies
//public AssemblyMirror assembly() {
//  return container().assembly();
//}
//
/// ** {@return the deployment this application is a part of.} * /
//public DeploymentMirror deployment() {
//  return application.deployment.mirror();
//}
//
//public Node<ApplicationMirror> deploymentNode() {
//  throw new UnsupportedOperationException();
//}
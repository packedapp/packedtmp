package app.packed.application;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.build.BuildGoal;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMetaHook.BindingTypeHook;
import app.packed.extension.ExtensionMirror;
import app.packed.lifetime.ContainerLifetimeMirror;
import app.packed.namespace.NamespaceMirror;
import app.packed.operation.OperationMirror;
import app.packed.service.ServiceContract;
import app.packed.util.TreeView;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ApplicationSetup;
import internal.app.packed.container.AssemblySetup.PackedAssemblyTreeMirror;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ContainerSetup.PackedContainerTreeMirror;
import internal.app.packed.operation.OperationSetup;

/**
 * A mirror of an application.
 * <p>
 * An application mirror instance is typically obtained by calling application mirror factory methods such as
 * {@link App#mirrorOf(Assembly, Wirelet...)}.
 * <p>
 * Instances of this class should never be created {@link #ApplicationMirror()} directly as the framework needs to
 * perform initialization before it can be used.
 * <p>
 * Instances of ApplicationMirror can be injected into any bean simply by declaring a dependency on this class.
 * <p>
 * Like many other mirrors classes the type of application mirror being returned can be specialized. See
 * {@link BootstrapApp.Composer#specializeMirror(java.util.function.Supplier)} for details.
 */
@BindingTypeHook(extension = BaseExtension.class)
public class ApplicationMirror implements ComponentMirror {

    /** The application we are mirroring. */
    private final ApplicationSetup application;

    /**
     * Create a new application mirror.
     *
     * @throws IllegalStateException
     *             if attempting to explicitly construct an application mirror instance
     */
    public ApplicationMirror() {
        // Will fail if the application mirror is not initialized by the framework
        this.application = ApplicationSetup.MIRROR_INITIALIZER.initialize();
    }

    /** {@return a tree representing all the assemblies used for creating this application.} */
    public AssemblyMirror.OfTree assemblies() {
        return new PackedAssemblyTreeMirror(application.container.assembly, null);
    }

    /** {@return the build goal that was used when building the application.} */
    public BuildGoal buildGoal() {
        return application.deployment.goal;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return application.componentPath();
    }

    /** {@return a mirror of the root container in the application.} */
    public ContainerMirror container() {
        return application.container.mirror();
    }

    /** {@return a container tree mirror representing all the containers defined within the application.} */
    public ContainerMirror.OfTree containers() {
        return new PackedContainerTreeMirror(application.container, null);
    }

    /** {@return a collection of all entry points the application may have.} */
    public Collection<OperationMirror> entryPoints() {
        return container().lifetime().entryPoints();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof ApplicationMirror m && application == m.application;
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

    /** {@return an unmodifiable {@link Set} view of every extension type that has been used in the application.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container().extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return application.hashCode();
    }

    /** {@return the application's lifetime. Which is identical to the root container's.} */
    public ContainerLifetimeMirror lifetime() {
        return container().lifetime();
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
        return application.container.node.name;
    }

    public <N extends NamespaceMirror<?>> N namespace(Class<N> type) {
        return namespace(type, "main");
    }

    public <N extends NamespaceMirror<?>> N namespace(Class<N> type, String name) {
        throw new UnsupportedOperationException();
    }

    public void print() {
        // Maybe return ApplicationPrinter???
        // to(PrintStream ps);
        // asJSON();
        // verbose();
        print0(application.container);
    }

    private void print0(ContainerSetup cs) {
        for (var e = cs.node.firstChild; e != null; e = e.node.nextSibling) {
            print0(e);
        }
        for (BeanSetup b : cs.beans) {
            StringBuilder sb = new StringBuilder();
            sb.append(b.componentPath()).append("");
            sb.append(" [").append(b.beanClass.getName()).append("], owner = " + b.author());
            sb.append("\n");
            for (OperationSetup os : b.operations.all) {
                // sb.append(" ".repeat(b.path().depth()));
                sb.append("    o ");
                sb.append(os.mirror());
                sb.append("\n");
            }
            System.out.print(sb.toString());
        }
    }

    /** {@return the service contract of this application.} */
    public ServiceContract serviceContract() {
        return application.container.sm.newContract();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Application:" + name();
    }

    /**
     * Returns an extension mirror of the specified type. Or fails by throwing {@link UnsupportedOperationException}.
     *
     * @param <T>
     *            The type of extension mirror
     * @param type
     *            The type of extension mirror
     * @return an extension mirror of the specified type
     *
     * @see ContainerMirror#use(Class)
     */
    public <E extends ExtensionMirror<?>> E use(Class<E> type) {
        return container().use(type);
    }

    public <E extends ExtensionMirror<?>> void useIfPresent(Class<E> type, Consumer<? super E> action) {
        throw new UnsupportedOperationException();
    }

    /**
    *
    */
    // Problemet med ApplicationTree er vel navngivning

    // Hvorfor ikke bare ApplicationMirror.ofTree eller inTree + InSet
    // Tror ikke denne bliver brugt mere
    public interface OfTree extends TreeView<ApplicationMirror> {

        /** {@return all the assemblies that make of the application.} */
        AssemblyMirror.OfTree assemblies();
    }

    // ApplicationMirror bootstappedBy(); // nah hvad hvis det er et child som root.
    // Maa have noget paa Application som Host? Ikke parent da det ikke er en Application

}

//
///** {@return a mirror of the root assembly that defines the application.} */
//// IDK if we want this or only assemblies
//public AssemblyMirror assembly() {
//  return container().assembly();
//}
//
///** {@return the deployment this application is a part of.} */
//public DeploymentMirror deployment() {
//  return application.deployment.mirror();
//}
//
//public Node<ApplicationMirror> deploymentNode() {
//  throw new UnsupportedOperationException();
//}
package app.packed.container;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.DeploymentMirror;
import app.packed.component.Mirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionMetaHook.BindingTypeHook;
import app.packed.util.TreeView;
import app.packed.util.TreeView.Node;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ContainerSetup.PackedContainerTreeMirror;

/**
 * A mirror of an assembly.
 * <p>
 * An instance of AssemblyMirror can be injected at runtime simply by declaring a dependency on it.
 *
 * @see ApplicationMirror#assembly()
 * @see ContainerMirror#assembly()
 */
@BindingTypeHook(extension = BaseExtension.class)
public class AssemblyMirror implements Mirror {

    /** The assembly we are mirroring. */
    private final AssemblySetup assembly;

    /**
     * Create a new assembly mirror.
     *
     * @throws IllegalStateException
     *             if attempting to explicitly construct an assembly mirror instance
     */
    public AssemblyMirror() {
        // Will fail if the assembly mirror is not initialized by the framework
        this.assembly = AssemblySetup.MIRROR_INITIALIZER.initialize();
    }

    /** {@return the application this assembly contributes to.} */
    public ApplicationMirror application() {
        return assembly.container.application.mirror();
    }

    /** {@return the node representing this assembly in the application's tree of assemblies.} */
    public TreeView.Node<AssemblyMirror> applicationNode() {
        throw new UnsupportedOperationException();
    }

    /** {@return the assembly class.} */
    public Class<? extends Assembly> assemblyClass() {
        return assembly.assembly.getClass();
    }

    /**
     * Returns the duration of the assemble phase for this assembly. This is roughly the time spent in the build method.
     * Added with time spend for each extension to calculate stuff. Time spend for an application may also. Ty
     * <p>
     * The duration reported by this method never include time spent on generating code. Code generation is always done on a
     * per application basis. And cannot be tracked on a per-assembly basis.
     * <p>
     * Durations reported when starting up a JVM is typically dominated by time spend loading classes.
     *
     * @return how much time was spend assembling.
     */
    public Duration assemblyDuration() {
        return Duration.ofNanos(Math.max(0, assembly.assemblyBuildFinishedTime - assembly.assemblyBuildStartedTime));
    }

    /** {@return a list of hooks that are applied to containers defined by the assembly.} */
    // TODO present on ContainerMirror as well? Maybe a ContainerHookMirror, I really think it should be
    // Would be nice to see if a given assembly hook was applied to the container.
    // And the order
    public List<Class<? extends AssemblyHook>> assemblyHooks() {
        return List.of(); // TODO implement
    }

    /**
     * {@return a stream of any child assemblies defined by this assembly.}
     *
     * @see ContainerConfiguration#link(Assembly, Wirelet...)
     */
    public Stream<AssemblyMirror> children() { // method should be aligned with other trees
        return children(assembly, assembly.container, new ArrayList<>()).stream();
    }

    private ArrayList<AssemblyMirror> children(AssemblySetup assembly, ContainerSetup cs, ArrayList<AssemblyMirror> list) {
        if (assembly == cs.assembly) {
            for (var e = cs.node.firstChild; e != null; e = e.node.nextSibling) {
                children(assembly, e, list);
            }

//            for (ContainerSetup c : cs.node().children()) {
//                //children(assembly, c, list);
//            }
        } else {
            list.add(cs.assembly.mirror());
        }
        return list;
    }

    /** {@return the root container this assembly defines.} */
    public ContainerMirror container() {
        return assembly.container.mirror();
    }

    /** {@return the tree of containers this assembly defines.} */
    public ContainerMirror.OfTree containers() {
        return new PackedContainerTreeMirror(assembly.container, c -> c.assembly == assembly);
    }

    public List<Class<? extends DelegatingAssembly>> delegatedFrom() {
        return assembly.delegatingAssemblies;
    }

    /** {@return the deployment this assembly is a part of.} */
    public DeploymentMirror deployment() {
        return assembly.container.application.deployment.mirror();
    }

    /** {@return the application this assembly contributes to.} */
    public Node<AssemblyMirror> deploymentNode() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof AssemblyMirror m && assembly == m.assembly;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return assembly.hashCode();
    }

    /** @return true if this assembly is top assembly, otherwise false. */
    // isApplicationRoot?
    public boolean isApplicationRoot() {
        return assembly.container.isApplicationRoot();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Assembly:" + application().name() + ":/";
    }

    /**
     * Represents a collection of assemblies that are ordered in a rooted tree.
     * <p>
     * This
     */

    // Multi app.
    // application.assemblies() All assemblies that make of the application. Child applications not included.

    // application.tree().assemblies() <--- Application tree for assemblies

    public interface OfTree extends TreeView<AssemblyMirror> {

        /**
         *
         */
        void print();

        void printWithDuration();
    }

}

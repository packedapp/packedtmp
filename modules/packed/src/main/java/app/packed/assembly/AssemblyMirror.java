package app.packed.assembly;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.bean.scanning.BeanTrigger.OnContextServiceVariable;
import app.packed.build.BuildCodeSourceMirror;
import app.packed.build.hook.BuildHookMirror;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.util.AnnotationList;
import app.packed.util.TreeView;
import app.packed.util.TreeView.Node;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.MirrorImplementationBeanIntrospector;
import internal.app.packed.util.PackedTreeView;

/**
 * A mirror of an assembly.
 * <p>
 * An instance of AssemblyMirror can be injected into a bean at runtime simply by declaring a dependency on it.
 * <p>
 * There are currently no support for allowing this class to be extended.
 *
 * @see ApplicationMirror#assembly()
 * @see ContainerMirror#assembly()
 */
@OnContextServiceVariable(introspector = MirrorImplementationBeanIntrospector.class)
public final class AssemblyMirror implements BuildCodeSourceMirror {

    /** The assembly we are mirroring. */
    private final AssemblySetup assembly;

    /**
     * Create a new assembly mirror.
     *
     * @throws IllegalStateException
     *             if attempting to explicitly construct an assembly mirror instance
     */
    AssemblyMirror(AssemblySetup assembly) {
        this.assembly = requireNonNull(assembly);
    }

    /**
     *
     * This list only containers relevant annotations that are understod by the framework. Or does it? I think it should
     * contain all annotations
     *
     * @return
     *
     * @see Class#getAnnotations()
     */
    // This list may be heavily changed by a delegating assembly
    public AnnotationList annotations() {
        throw new UnsupportedOperationException();
    }

    /** {@return the application this assembly contributes to.} */
    public ApplicationMirror application() {
        return assembly.container.application.mirror();
    }

    /** {@return the node representing this assembly in the application's tree of assemblies.} */
    // alternativ. application.assemblies().find(node).get();
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

    /** {@return a stream of all build hooks that have been applied to the assembly} */
    public Stream<BuildHookMirror> buildHooks() {
        throw new UnsupportedOperationException();
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
            for (var e = cs.treeFirstChild; e != null; e = e.treeNextSibling) {
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

    /** {@return a list of hooks that are applied to containers defined by the assembly.} */
    // TODO present on ContainerMirror as well? Maybe a ContainerHookMirror, I really think it should be
    // Would be nice to see if a given assembly hook was applied to the container.
    // And the order
//    public List<Class<? extends TransformAssembly>> assemblyHooks() {
//        return List.of(); // TODO implement
//    }

    // do we need a allComponents() that includes not-developer components
    public TreeView<ComponentMirror> components() {
        // Ideen er vi itererer over alle componenter
        // Men ApplicationMirror<-AssemblyMirror<-ContainerMirror
        // AssemblyMirror er lidt dum... Maaske er det ikke et trae
        throw new UnsupportedOperationException();
    }

    /** {@return the root container this assembly defines.} */
    // I think remove it from now, and replace with containers().root()
    public ContainerMirror container() {
        return assembly.container.mirror();
    }

    /** {@return the tree of containers this assembly defines.} */
    public TreeView<ContainerMirror> containers() {
        return new PackedTreeView<>(assembly.container, c -> c.assembly == assembly, c -> c.mirror());
    }

    public Stream<BuildHookMirror> declaredBuildHooks() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * This list only contains assembly build hooks that are left after
     * {@link AssemblyBuildHook#transformBuildHooks(AssemblyDescriptor, List)} has been executed for all assembly hooks
     *
     * @return
     */
    public List<BuildHookMirror> declaredBuildHooks2() {
        return List.of();
    }

    public List<Class<? extends DelegatingAssembly>> delegatedFrom() {
        return assembly.delegatingAssemblies;
    }

    /** {@return the application this assembly contributes to.} */
    public Node<AssemblyMirror> deploymentNode() {
        throw new UnsupportedOperationException();
    }

//    /** {@return the deployment this assembly is a part of.} */
//    public DeploymentMirror deployment() {
//        return assembly.container.application.deployment.mirror();
//    }

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

    /** {@return the security model for the assembly} */
    public AssemblySecurity securityModel() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Assembly:" + application().name() + ":/";
    }
}

package app.packed.container;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.DeploymentMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.BindingTypeHook;
import app.packed.util.TreeNavigator;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.Mirror;

/**
 * A mirror of an application.
 * <p>
 * An instance of this class is typically obtained by calling a application mirror factory method such as
 * {@link App#mirrorOf(Assembly, Wirelet...)}.
 * <p>
 * An instance of ApplicationMirror can be injected at runtime simply by declaring a dependency on it.
 * <p>
 * Instances of this class can only be constructed by the framework
 */
@BindingTypeHook(extension = BaseExtension.class)
public final class AssemblyMirror implements Mirror {

    /** The internal configuration of the assembly we are mirroring. */
    private final AssemblySetup assembly = AssemblySetup.MIRROR_INITIALIZER.initialize();

    /**
     * Create a new application mirror.
     *
     * @throws IllegalStateException
     *             if attempting to explicitly construct an assembly mirror instance
     */
    public AssemblyMirror() {}

    /** {@return the application this assembly contributes to.} */
    public ApplicationMirror application() {
        return assembly.container.application.mirror();
    }

    /** {@return the application this assembly contributes to.} */
    public TreeNavigator<AssemblyMirror> applicationNode() {
        throw new UnsupportedOperationException();
    }

    /** {@return the assembly class.} */
    public Class<? extends Assembly> assemblyClass() {
        return assembly.assembly.getClass();
    }

    /**
     * Returns the duration of the assemble phase for this assembly. This is roughly the time spent in the build method.
     * Added with time spend for each extension to calculate stuff.
     * <p>
     * The duration reported by this method never include time spent on generating code. Code generation is always done on a
     * per application basis. And cannot be tracked on a per-assembly basis.
     * <p>
     * Durations reported when starting up a JVM is typically dominated by time spend loading classes.
     *
     * @return how must time was spend assembling.
     */
    public Duration assemblyDuration() {
        return Duration.ofNanos(Math.max(0, assembly.assemblyFinished - assembly.assemblyStart));
    }

    /** {@return a list of hooks that are applied to containers defined by the assembly.} */
    // present on ContainerMirror as well? Maybe a ContainerHookMirror, I really think it should be
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
            for (var e = cs.treeFirstChild; e != null; e = e.treeNextSibling) {
                children(assembly, e, list);
            }
        } else {
            list.add(cs.assembly.mirror());
        }
        return list;
    }

    /** {@return the root container this assembly defines.} */
    public ContainerMirror container() {
        return assembly.container.mirror();
    }

    /** {@return a tree of all the containers this assembly defines.} */
    public ContainerTreeMirror containers() {
        throw new UnsupportedOperationException();
    }

    public List<Class<? extends DelegatingAssembly>> delegatedFrom() {
        return assembly.delegatingAssemblies;
    }

    /** {@return the deployment this assembly is a part of.} */
    public DeploymentMirror deployment() {
        return assembly.container.application.deployment.mirror();
    }

    /** {@return the application this assembly contributes to.} */
    public TreeNavigator<AssemblyMirror> deploymentNode() {
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Assembly:" + application().name() + ":/";
    }
}

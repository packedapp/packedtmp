package app.packed.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.App;
import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.extension.MirrorExtension;
import app.packed.framework.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.Mirror;

/**
 * A mirror of an application.
 * <p>
 * An instance of this class is typically obtained by calling a application mirror factory method such as
 * {@link App#newMirror(Assembly, Wirelet...)}.
 * <p>
 * Instances of ApplicationMirror can be injected at runtime simply by declaring a dependency on it. This will
 * automatically install the {@link MirrorExtension} which will provide an instance at runtime.
 * <p>
 * Like many other mirrors this class is overridable via
 * {@link ApplicationDriver.Builder#specializeMirror(java.util.function.Supplier)}
 */
@BindingHook(extension = MirrorExtension.class)
public class AssemblyMirror implements Mirror {

    /**
     * The internal configuration of the application we are mirrored. Is initially null but populated via
     * {@link #initialize(ApplicationSetup)}.
     */
    @Nullable
    private AssemblySetup assembly;

    /**
     * Create a new application mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public AssemblyMirror() {}

    /** {@return the application this assembly contributes to.} */
    public ApplicationMirror application() {
        return assembly().application.mirror();
    }

    /**
     * {@return the internal configuration of application.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(ApplicationSetup)} has not been called.
     */
    private AssemblySetup assembly() {
        AssemblySetup a = assembly;
        if (a == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return a;
    }

    /** {@return the assembly class.} */
    public Class<? extends Assembly> assemblyClass() {
        return assembly().assembly.getClass();
    }

    /**
     * {@return a stream of any child assemblies defined by this assembly.}
     * 
     * @see ContainerConfiguration#link(Assembly, Wirelet...)
     */
    public Stream<AssemblyMirror> children() { // method should be aligned with other trees
        return children(assembly(), assembly().container, new ArrayList<>()).stream();
    }

    private ArrayList<AssemblyMirror> children(AssemblySetup assembly, ContainerSetup cs, ArrayList<AssemblyMirror> list) {
        if (assembly == cs.assembly) {
            for (var e = cs.treeFirstChild; e != null; e = e.treeNextSiebling) {
                children(assembly, e, list);
            }
        } else {
            list.add(cs.assembly.mirror());
        }
        return list;
    }

    /** {@return the container that is defined by this assembly.} */
    public ContainerMirror container() {
        return assembly().container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof AssemblyMirror m && assembly() == m.assembly();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return assembly().hashCode();
    }

    /** {@return a list of hooks that are applied to containers defined by the assembly.} */
    // present on ContainerMirror as well? Maybe a ContainerHookMirror, I really think it should be
    public List<Class<? extends AssemblyHook>> hooks() {
        return List.of(); // TODO implement
    }

    /**
     * Invoked by {@link AssemblySetup#mirror()} to initialize this mirror.
     * 
     * @param assembly
     *            the internal configuration of the application to mirror
     */
    final void initialize(AssemblySetup assembly) {
        if (this.assembly != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.assembly = assembly;
    }

    /** @return whether or not this assembly defines the root container in the application.} */
    public boolean isRoot() {
        return assembly().container.treeParent == null;
    }

    /**
     * {@return the parent of this assembly, or empty if the assembly defines the root container of the application.}
     */
    public Optional<AssemblyMirror> parent() {
        ContainerSetup org = assembly().container;
        for (ContainerSetup p = org.treeParent; p != null; p = p.treeParent) {
            if (org.assembly != p.assembly) {
                return Optional.of(p.assembly.mirror());
            }
        }
        return Optional.empty();
    }

    public List<Class<? extends DelegatingAssembly>> delegatedFrom() {
        return List.of();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Assembly";
    }
}

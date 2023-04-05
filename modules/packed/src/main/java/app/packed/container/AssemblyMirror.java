package app.packed.container;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.BindingTypeHook;
import app.packed.util.Nullable;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.TreeMirror;

/**
 * A mirror of an application.
 * <p>
 * An instance of this class is typically obtained by calling a application mirror factory method such as
 * {@link App#mirrorOf(Assembly, Wirelet...)}.
 * <p>
 * An instance of ApplicationMirror can be injected at runtime simply by declaring a dependency on it.
 */
@BindingTypeHook(extension = BaseExtension.class)
public final class AssemblyMirror implements TreeMirror<AssemblyMirror> {

    /**
     * The internal configuration of the application we are mirrored. Is initially {@code null} but populated via
     * {@link #initialize(ApplicationSetup)}
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
        return assembly().container.application.mirror();
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
        AssemblySetup a = assembly();
        return Duration.ofNanos(Math.max(0, a.assemblyFinished - a.assemblyStart));
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
    @Override
    public Stream<AssemblyMirror> children() { // method should be aligned with other trees
        return children(assembly(), assembly().container, new ArrayList<>()).stream();
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

    /** {@return the container that is defined by this assembly.} */
    public ContainerMirror container() {
        return assembly().container.mirror();
    }

    public List<Class<? extends DelegatingAssembly>> delegatedFrom() {
        return assembly().delegatingAssemblies;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof AssemblyMirror m && assembly() == m.assembly();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
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
    void initialize(AssemblySetup assembly) {
        if (this.assembly != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.assembly = assembly;
    }

    /** @return whether or not this assembly defines the root container in the application.} */
    public boolean isRoot() {
        return assembly().container.isApplicationRoot();
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Assembly";
    }
}

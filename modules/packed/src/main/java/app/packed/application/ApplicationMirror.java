package app.packed.application;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.BindingTypeHook;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.lifetime.ContainerLifetimeMirror;
import app.packed.util.Nullable;
import internal.app.packed.container.ApplicationSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.Mirror;
import internal.app.packed.operation.OperationSetup;

/**
 * A mirror of an application.
 * <p>
 * An application mirror instance is typically obtained by calling application mirror factory methods such as
 * {@link App#mirrorOf(Assembly, Wirelet...)}.
 * <p>
 * Instances of ApplicationMirror can be injected at runtime simply by declaring a dependency on it.
 * <p>
 * Like many other mirrors classes the type of application mirror being returned can be specialized using
 * {@link BootstrapApp.Composer#specializeMirror(java.util.function.Supplier)}.
 */
@BindingTypeHook(extension = BaseExtension.class)
public class ApplicationMirror implements Mirror {

    /** The configuration of the application. Is initially null but populated via {@link #initialize(ApplicationSetup)}. */
    @Nullable
    private ApplicationSetup application;

    /** Create a new application mirror. */
    public ApplicationMirror() {}

    /**
     * {@return the internal configuration of application.}
     *
     * @throws IllegalStateException
     *             if {@link #initialize(ApplicationSetup)} has not been called.
     */
    private ApplicationSetup application() {
        ApplicationSetup a = application;
        if (a == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return a;
    }

    /** {@return a mirror of the assembly that defines the application.} */
    public AssemblyMirror assembly() {
        return container().assembly();
    }

    /** {@return the build goal that was used when building the application.} */
    public BuildGoal buildGoal() {
        return application().goal;
    }

    /** {@return a mirror of the root container in the application.} */
    public ContainerMirror container() {
        return application().container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof ApplicationMirror m && application() == m.application();
    }

    /** {@return an unmodifiable {@link Set} view of every extension type that has been used in the application.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container().extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return application().hashCode();
    }

    /**
     * Invoked by {@link ApplicationSetup#mirror()} to initialize this mirror.
     *
     * @param application
     *            the internal configuration of the application to mirror
     */
    final void initialize(ApplicationSetup application) {
        if (this.application != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.application = application;
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
        return application.container.name;
    }

    public void print() {
        print0(application.container);
    }

    private void print0(ContainerSetup cs) {
        for (var e = cs.treeFirstChild; e != null; e = e.treeNextSiebling) {
            print0(e);
        }
        for (var b = cs.beanFirst; b != null; b = b.beanSiblingNext) {
            StringBuilder sb = new StringBuilder();
            sb.append(b.path()).append("");
            sb.append(" [").append(b.beanClass.getName()).append("], owner = " + b.owner());
            sb.append("\n");
            for (OperationSetup os : b.operations) {
                // sb.append(" ".repeat(b.path().depth()));
                sb.append("    o ");
                sb.append(os.mirror());
                sb.append("\n");
            }
            System.out.print(sb.toString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Application";
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
}


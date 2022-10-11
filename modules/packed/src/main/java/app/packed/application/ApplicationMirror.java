package app.packed.application;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import app.packed.container.Wirelet;
import app.packed.lifetime.ContainerLifetimeMirror;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.Mirror;

/**
 * A mirror of an application.
 * <p>
 * An instance of this class is typically obtained by calling a application mirror factory method such as
 * {@link App#newMirror(Assembly, Wirelet...)}.
 * 
 * <p>
 * Like most other mirrors this class is overridable via
 * {@link ApplicationDriver.Builder#specializeMirror(java.util.function.Supplier)}
 */
public class ApplicationMirror implements Mirror {

    /**
     * The internal configuration of the application we are mirrored. Is initially null but populated via
     * {@link #initialize(ApplicationSetup)}.
     */
    @Nullable
    private ApplicationSetup application;

    /**
     * Create a new application mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
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

    /** {@return the build goal used when building the application.} */
    public BuildGoal buildGoal() {
        return application().goal();
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

    /** {@return a {@link Set} view of every extension type that has been used in the application.} */
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
        return application.container.getName();
    }

    public void print() {
        print0(application.container);
    }

    private void print0(ContainerSetup cs) {
        System.out.println(cs.path());
        for (var e = cs.treeFirstChild; e != null; e = e.treeNextSiebling) {
            print0(e);
        }
        for (Object b : cs.children.values()) {
            if (b instanceof BeanSetup bs) {
                StringBuilder sb = new StringBuilder();
                sb.append(bs.path()).append("");
                sb.append(" [").append(bs.beanClass().getName()).append("], owner = " + bs.owner());
                System.out.println(sb.toString());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Application";
    }

    /**
     * Returns an extension mirror of the specified type.
     * 
     * @param <T>
     *            The type of extension mirror
     * @param type
     *            The type of extension mirror
     * @return an extension mirror of the specified type
     * 
     * @see ContainerMirror#useExtension(Class)
     */
    // Maasker drop use, og bare have extension(ServiceExtensionMirror.class).
    public <E extends ExtensionMirror<?>> E useExtension(Class<E> type) {
        return container().useExtension(type);
    }

    public <E extends ExtensionMirror<?>> void useIfPresent(Class<E> type, Consumer<? super E> action) {
        throw new UnsupportedOperationException();
    }
}

//default <T extends ComponentMirror> SetView<T> findAll(Class<T> componentType, boolean includeChildApplications) {
//    throw new UnsupportedOperationException();
//}

//// Relations between to different applications
//// Ret meget som ComponentRelation
//
///// Maaske flyt til ApplicationMirror.relation...
///// Der er ingen der kommer til at lave dem selv...

//default <T extends ComponentMirror> Stream<T> select(Class<T> componentType) {
//  throw new UnsupportedOperationException();
//}

//default TreeWalker<ApplicationMirror> walker() {
//    throw new UnsupportedOperationException();
//    // app.components() <-- all component in the application
//    // app.component().walker() <--- all components application or not...
//
//    // someComponent.walker().filter(c->c.application == SomeApp)...
//}

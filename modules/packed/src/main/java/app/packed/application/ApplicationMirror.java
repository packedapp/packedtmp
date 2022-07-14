package app.packed.application;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import app.packed.container.Wirelet;
import app.packed.lifetime.LifetimeMirror;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.Mirror;

/**
 * A mirror of an application.
 * <p>
 * An instance of this class is typically obtained by calling a application mirror factory method such as
 * {@link App#mirror(Assembly, Wirelet...)}.
 */
public class ApplicationMirror implements Mirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(ExtensionSetup)}.
     */
    @Nullable
    private ApplicationSetup application;

    /**
     * Create a new operation mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public ApplicationMirror() {}

    /**
     * {@return the internal configuration of operation.}
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

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof ApplicationMirror m && application() == m.application();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return application().hashCode();
    }

    /**
     * Invoked by {@link ApplicationSetup#mirror()} to set up this mirror.
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

    /** {@return the assembly that defines the application.} */
    public AssemblyMirror assembly() {
        return container().assembly();
    }

    /** {@return the root container in the application.} */
    public ContainerMirror container() {
        return application().container.mirror();
    }

    /** {@return a descriptor for the application.} */
    public ApplicationBuildInfo descriptor() {
        return application().info;
    }

    /** {@return a {@link Set} view of every extension type that has been used in the container.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container().extensionTypes();
    }

    /** {@return the application's lifetime.} */
    public LifetimeMirror lifetime() {
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
        return container().name();
    }

    public void print() {
        container().stream().forEach(cc -> {
            StringBuilder sb = new StringBuilder();
            sb.append(cc.path()).append("");
            if (cc instanceof BeanMirror bm) {
                sb.append(" [").append(bm.beanClass().getName()).append("], owner = " + bm.owner());
            }
            System.out.println(sb.toString());
        });
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Application";
    }

    /**
     * @param <T>
     *            The type of extension mirror
     * @param type
     *            The type of extension mirror
     * @return an extension mirror of the specified type
     * 
     * @see ContainerMirror#useExtension(Class)
     */
    // Maasker drop use, og bare have extension(ServiceExtensionMirror.class).
    public <T extends ExtensionMirror<?>> T useExtension(Class<T> type) {
        return container().useExtension(type);
    }
}
//En application kan
////Vaere ejet af bruger
////Member of an extension (neeej sjaeldent, if ever...)
////Controlled by an extension

//Fx Session er controlled by WebExtension men er ikke member af den

//// Tror det ville giver mening at have OperationMirrorList her...
//// Kan ogsaa vaere vi bare skal smide den paa ComponentMirrorTree...
//default <T extends OperationMirror> Collection<T> operations(Class<T> operationType) {
//    throw new UnsupportedOperationException();
//}
///**
// * {@return the module of the application. This is always the module of the Assembly or ComposerAction class that
// * defines the application container.}
// * 
// * Altsaa hvis en application skal have et module... Skal container+Bean vel ogsaa
// */
//// Hmm, hvis applikation = Container specialization... Ved component
//// Tror maaske ikke vi vil have den her, IDK... HVad med bean? er det realm eller bean module
//// Maaske vi skal have et realm mirror????
//Module module();

//default <T extends ComponentMirror> SetView<T> findAll(Class<T> componentType, boolean includeChildApplications) {
//    throw new UnsupportedOperationException();
//}
//
//// Relations between to different applications
//// Ret meget som ComponentRelation
//
///// Maaske flyt til ApplicationMirror.relation...
///// Der er ingen der kommer til at lave dem selv...
//
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

//
///**
// * Returns an immutable set containing any extensions that have been disabled.
// * 
// * @return an immutable set containing any extensions that have been disabled
// * 
// * @see ApplicationDriver.Builder#disableExtension(Class...)
// */
//Set<Class<? extends Extension<?>>> disabledExtensions();
package app.packed.application;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.various.TaskListMirror;
import app.packed.component.ComponentMirror;
import app.packed.container.Assembly;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.mirror.Mirror;
import app.packed.mirror.SetView;
import app.packed.mirror.TreeWalker;
import packed.internal.application.PackedApplicationDriver;

/**
 * A mirror of an application.
 * <p>
 * An instance of this class is typically obtained by calling {@link #of(Assembly, Wirelet...)} on this class.
 */
// En application kan
//// Vaere ejet af bruger
//// Member of an extension (neeej sjaeldent, if ever...)
//// Controlled by an extension

// Fx Session er controlled by WebExtension men er ikke member af den
public interface ApplicationMirror extends Mirror {


    /** {@return the root container in the application.} */
    ContainerMirror container();

    /** {@return a descriptor for the application.} */
    ApplicationDescriptor descriptor();

    /** {@return a {@link Set} view of every extension type that has been used in the container.} */
    default Set<Class<? extends Extension<?>>> extensionTypes() {
        return container().extensionTypes();
    }

    
    /**
     * Returns an immutable set containing any extensions that have been disabled.
     * 
     * @return an immutable set containing any extensions that have been disabled
     * 
     * @see ApplicationDriver.Builder#disableExtension(Class...)
     */
    Set<Class<? extends Extension<?>>> disabledExtensions();

    default <T extends ComponentMirror> SetView<T> findAll(Class<T> componentType, boolean includeChildApplications) {
        throw new UnsupportedOperationException();
    }

    default void forEachComponent(Consumer<? super ComponentMirror> action) {
        container().components().forEach(action);
    }

    default TaskListMirror initialization() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return the module of the application. This is always the module of the Assembly or ComposerAction class that
     * defines the application container.}
     */
    // Hmm, hvis applikation = Container specialization... Ved component
    // Tror maaske ikke vi vil have den her, IDK... HVad med bean? er det realm eller bean module
    // Maaske vi skal have et realm mirror????
    Module module();

    /**
     * Returns the name of the application.
     * <p>
     * The name of an application is always identical to the name of the root container.
     * 
     * @return the name of the application
     * @see Assembly#named(String)
     * @see Wirelet#named(String)
     */
    default String name() {
        return container().name();
    }

    default void print() {
        container().print();
    }

    default <T extends ComponentMirror> Stream<T> select(Class<T> componentType) {
        throw new UnsupportedOperationException();
    }

    <T extends ExtensionMirror> T use(Class<T> type);

    // Relations between to different applications
    // Ret meget som ComponentRelation

    /// Maaske flyt til ApplicationMirror.relation...
    /// Der er ingen der kommer til at lave dem selv...

    default TreeWalker<ApplicationMirror> walker() {
        throw new UnsupportedOperationException();
        // app.components() <-- all component in the application
        // app.component().walker() <--- all components application or not...

        // someComponent.walker().filter(c->c.application == SomeApp)...
    }

    /**
     * Create
     * 
     * @param assembly
     *            the assembly containing the application to create a mirror for
     * @param wirelets
     *            optional wirelets
     * @return an application mirror
     */
    // IDK om vi bare altid bruger en Application Launcher class...
    public static ApplicationMirror of(Assembly assembly, Wirelet... wirelets) {
        return PackedApplicationDriver.MIRROR_DRIVER.mirrorOf(assembly, wirelets);
    }
    
    default <T extends ExtensionMirror> T useExtension(Class<T> extensionMirrorType) {
        return container().useExtension(extensionMirrorType);
    }
}

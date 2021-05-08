package app.packed.application;

import java.util.Optional;

import app.packed.base.NamespacePath;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.mirror.Mirror;
import app.packed.mirror.TreeMirrorWalker;

/**
 * A mirror of an application.
 * <p>
 */
public interface ApplicationMirror extends Mirror {

    /** {@return the root component in the application}. */
    ComponentMirror component();

    // Er det kun componenter i den application??? Ja ville jeg mene...
    // Men saa kommer vi ud i spoergsmaalet omkring er components contextualizable...
    // app.rootContainer.children() <-- does this only include children in the same
    // application?? or any children...

    /** {@return the component in the application}. */
    ComponentMirror component(CharSequence path);

    TreeMirrorWalker<ComponentMirror> components();

    /** {@return the root container in the application}. */
    ContainerMirror container();

    // teanker det kun er containere i samme application...
    // ellers maa man bruge container.resolve("....")
    ContainerMirror container(CharSequence path);

    /** {@return a walker containing all the containers in this application} */
    TreeMirrorWalker<ComponentMirror> containers();

    default TaskListMirror initialization() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or the application is runnable. The value is always determined by
     * {@link ApplicationDriver#hasRuntime()}.
     * 
     * @return whether or the application is runnable
     */
    boolean isRunnable();

    // Wired er parent component<->child component
    // connections er component til any component.
    /**
     * Returns whether or not this application is strongly wired to a parent application.
     * <p>
     * A root application will always return false.
     * 
     * @return {@code true} if this application is strongly wired to a parent application, otherwise {@code false}
     */
    boolean isStronglyWired();

    /**
     * {@return the module that the application belongs to. This is typically the module of the assembly that defined the
     * root container.}
     */
    Module module();

    /** {@return the name of the application.} */
    String name();

    /** {@return the parent application of this application. Or empty if this application has no parent} */
    Optional<ApplicationMirror> parent();

    NamespacePath path();
    // Optional<ApplicationRelation> parentRelation();

    default TreeMirrorWalker<ApplicationMirror> walker() {
        throw new UnsupportedOperationException();
        // app.components() <-- all component in the application
        // app.component().walker() <--- all components application or not...

        // someComponent.walker().filter(c->c.application == SomeApp)...
    }
}

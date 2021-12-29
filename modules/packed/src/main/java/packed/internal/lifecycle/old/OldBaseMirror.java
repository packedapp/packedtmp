package packed.internal.lifecycle.old;

import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.various.TaskListMirror;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;

/**
 * A model of a (successful) build.
 */
public interface OldBaseMirror /* extends Mirror */ {

    /** {@return the root application of the build}. */
    ApplicationMirror application();

    ApplicationMirror application(CharSequence path);

    /** {@return a component stream that includes every component in this build}. */
    Stream<ApplicationMirror> applications();

    /** {@return the root component of the build}. */
    ComponentMirror component();

    ApplicationMirror component(CharSequence path);

    /** {@return a component stream that includes every component in this build}. */
    //ComponentMirrorStream components();

    /** {@return the root container of the build}. */
    ContainerMirror container();

    ContainerMirror container(CharSequence path);

    /** {@return a component stream that includes every component in this build}. */
    Stream<ContainerMirror> containers();

    default TaskListMirror initialization() {
        throw new UnsupportedOperationException();
    }

    default Set<Class<? extends Extension<?>>> extensions() {
        throw new UnsupportedOperationException();
    }
    
    /** { @return the name of the root application.} */
    default String name() {
        return application().name();
    }

}

// isClosedWorld -> No Deploy hosts models
// isOpenWorld -> Some hosts somewhere 

// analyze -> Application

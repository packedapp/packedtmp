package app.packed.application;

import java.util.stream.Stream;

import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.container.ContainerMirror;
import app.packed.inject.ServiceExtension;
import app.packed.mirror.Mirror;

/**
 * A model of a (successful) build.
 */
public interface BuildMirror extends Mirror {

    /** {@return the root application of the build}. */
    ApplicationMirror application();

    ApplicationMirror application(CharSequence path);

    /** {@return a component stream that includes every component in this build}. */
    Stream<ApplicationMirror> applications();

    /** {@return the root component of the build}. */
    ComponentMirror component();

    ApplicationMirror component(CharSequence path);

    /** {@return a component stream that includes every component in this build}. */
    ComponentMirrorStream components();

    /** {@return the root container of the build}. */
    ContainerMirror container();

    ContainerMirror container(CharSequence path);

    /** {@return a component stream that includes every component in this build}. */
    Stream<ContainerMirror> containers();

    /**
     * Returns the set of modifiers used for this assembling.
     * <p>
     * The returned set will always contain the {@link ComponentModifier#BUILD} modifier.
     * 
     * @return a set of modifiers
     */
    default ComponentModifierSet modifiers() {
        return component().modifiers();
    }

    /** {@return the build target.} */
    BuildTarget target();
}

// isClosedWorld -> No Deploy hosts models
// isOpenWorld -> Some hosts somewhere 

// analyze -> Application

class Doo {

    void Foo(BuildMirror m) {
        System.out.println("Number of applications in build" + m.applications().count());
        System.out.println("Number of components in build" + m.components().count());
        if (m.container("wef/123").hasExtension(ServiceExtension.class)) {

        }

        m.containers().filter(c -> c.hasExtension(ServiceExtension.class)).forEach(c -> System.out.println(c));

    }
}

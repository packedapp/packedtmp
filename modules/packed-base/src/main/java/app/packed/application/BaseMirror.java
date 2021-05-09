package app.packed.application;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.component.Assembly;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.inject.ServiceExtension;
import app.packed.mirror.Mirror;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.component.PackedComponentModifierSet;

/**
 * A model of a (successful) build.
 */
public interface BaseMirror extends Mirror {

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

    default void forEachComponent(Consumer<? super ComponentMirror> action) {
        components().forEach(action);
    }
    
    default TaskListMirror initialization() {
        throw new UnsupportedOperationException();
    }

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

    default Set<Class<? extends Extension>> extensions() {
        throw new UnsupportedOperationException();
    }
    
    /** { @return the name of the root application.} */
    default String name() {
        return application().name();
    }

    /** {@return the build target.} */
    BuildTarget target();
    

    // reflector
    public static ApplicationDriver<?> defaultDriver() {
        return PackedApplicationDriver.MIRROR_DRIVER;
    }
    
    static BaseMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return PackedApplicationDriver.MIRROR_DRIVER.build(assembly, wirelets, PackedComponentModifierSet.I_MIRROR).mirror();
    }
}

// isClosedWorld -> No Deploy hosts models
// isOpenWorld -> Some hosts somewhere 

// analyze -> Application

class Doo {

    void Foo(BaseMirror m) {
        System.out.println("Number of applications in build" + m.applications().count());
        System.out.println("Number of components in build" + m.components().count());
        if (m.container("wef/123").isUsed(ServiceExtension.class)) {

        }

        m.containers().filter(c -> c.isUsed(ServiceExtension.class)).forEach(c -> System.out.println(c));

    }
}

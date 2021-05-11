package app.packed.container;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.BaseMirror;
import app.packed.base.NamespacePath;
import app.packed.component.Assembly;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;
import app.packed.mirror.Mirror;

/**
 * A mirror of a container.
 * <p>
 * An instance of this class is typically opb
 */
public interface ContainerMirror extends Mirror /* extends Iterable<ComponentMirror> */ {

    /** {@return the application this container is a part of} */
    ApplicationMirror application();

    /** {@return an unmodifiable view of all of this container's children.} */
    // Giver det mening at det er paa kryds af apps?? Ja ville jeg mene
    Collection<ContainerMirror> children();

    /** {@return the root container component in the container} */
    ComponentMirror component();

    default Stream<ComponentMirror> components() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the distance to the root container. The root container having depth 0.
     * 
     * @return the distance to the root container
     */
    int depth();

    /** { @return a set of all extensions that have been used by the container.} */
    Set<Class<? extends Extension>> extensions();

    default void forEachComponent(Consumer<? super ComponentMirror> action) {
        components().forEach(action);
    }

    /**
     * Returns whether or not the container uses an extension of the specified type.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if this container uses an extension of the specified type
     */
    boolean isUsed(Class<? extends Extension> extensionType);

    /** {@return the name of the container.} */
    String name();

    /** {@return the parent container of this container. Or empty if this container has no parent} */
    Optional<ContainerMirror> parent();

    /** {@return the path of this container in relation to other containers} */
    NamespacePath path();

    default <T extends ExtensionMirror<?>> Optional<T> tryUse(Class<T> extensionMirrorType) {
        throw new UnsupportedOperationException();
    }

    // must be a concrete class taking the Extension as parameter
    default <T extends ExtensionMirror<?>> T use(Class<T> extensionMirrorType) {
        return tryUse(extensionMirrorType).orElseThrow();
    }

    public static ContainerMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return BaseMirror.of(assembly, wirelets).container();
    }
}

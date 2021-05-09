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
 * An instance of this class is typically opta
 */
public interface ContainerMirror extends Mirror {

    /** {@return the application this container is a part of} */
    ApplicationMirror application();

    /** {@return an unmodifiable view of all of this container's children} */
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

    /** { @return an unchangeable set of all extensions that are in use.} */
    Set<Class<? extends Extension>> extensions();

    default void forEachComponent(Consumer<? super ComponentMirror> action) {
        components().forEach(action);
    }

    /**
     * Returns whether or not the container contains an extension of the specified type.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if this container contains an extension of the specified type
     */
    boolean hasExtension(Class<? extends Extension> extensionType);

    /** {@return the name of the container} */
    String name();

    /** {@return the parent container of this container. Or empty if this container has no parent} */
    Optional<ContainerMirror> parent();

    /** {@return the path of this container in relation to other containers} */
    NamespacePath path();

    default <T extends SpecificExtensionMirror> T use(Class<T> extensionMirrorType) {
        throw new UnsupportedOperationException();
    }
    
    default <T extends SpecificExtensionMirror> Optional<T> find(Class<T> extensionMirrorType) {
        throw new UnsupportedOperationException();
    }
    
    public static ContainerMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return BaseMirror.of(assembly, wirelets).container();
    }
}

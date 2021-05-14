package app.packed.container;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.BaseMirror;
import app.packed.component.Assembly;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;

/**
 * A mirror of a container.
 * <p>
 * An instance of this class is typically opb
 */
// Kunne maaske godt taenke mig at extende ComponentMirror...
// Det giver bare saa meget mening for BeanMirror
public interface ContainerMirror extends ComponentMirror /* extends Iterable<ComponentMirror> */ {

    /** {@return an unmodifiable view of all of this container's children.} */
    // Giver det mening at det er paa kryds af apps?? Ja ville jeg mene
    Collection<ContainerMirror> containerChildren();

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
    int containerDepth();

    /** { @return a set of all extensions that have been used by the container.} */
    Set<Class<? extends Extension>> extensions();

    default void forEachComponent(Consumer<? super ComponentMirror> action) {
        components().forEach(action);
    }

    /** {@return whether or not the container is root container in an application.} */
    default boolean isApplicationContainer() {
        return application().container().equals(this);
    }

    /**
     * Returns whether or not the underlying container uses an extension of the specified type.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if this container uses an extension of the specified type
     */
    boolean isUsed(Class<? extends Extension> extensionType);

    /** {@return the parent container of this container. Or empty if this container has no parent} */
    Optional<ContainerMirror> containerParent();

    // Altsaa hvor brugbar er denne... Ved man
    <T extends ExtensionMirror<?>> Optional<T> tryUse(Class<T> extensionMirrorType); // maybe just find? find

    /**
     * Returns an mirror of the specified type, iff the mirror's extension type is in use by the underlying container.
     * 
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the type of mirror to return
     * @return an extension mirror of the specified type
     * @see ContainerConfiguration#use(Class)
     * @throws NoSuchElementException
     *             if the mirror's extension is not used by the container
     */
    default <T extends ExtensionMirror<?>> T use(Class<T> extensionMirrorType) {
        return tryUse(extensionMirrorType).orElseThrow();
    }

    public static ContainerMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return BaseMirror.of(assembly, wirelets).container();
    }
}

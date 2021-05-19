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
 * A mirror of a container (component).
 * <p>
 * An instance of this class is typically opb
 */
// Kunne maaske godt taenke mig at extende ComponentMirror...
// Det giver bare saa meget mening for BeanMirror
public interface ContainerMirror extends ComponentMirror /* extends Iterable<ComponentMirror> */ {

    default Stream<ComponentMirror> components() {
        throw new UnsupportedOperationException();
    }

    /** {@return an unmodifiable view of all of this container's children.} */
    // Giver det mening at det er paa kryds af apps?? Ja ville jeg mene
    Collection<ContainerMirror> containerChildren();

    /**
     * Returns the distance to the root container. The root container having depth 0.
     * 
     * @return the distance to the root container
     */
    int containerDepth();

    /** {@return the parent container of this container. Or empty if this container has no parent} */
    Optional<ContainerMirror> containerParent();

    /** {@return a set of all extensions that are used by the container.} */
    Set<ExtensionMirror<?>> extensions();

    default void forEachComponent(Consumer<? super ComponentMirror> action) {
        components().forEach(action);
    }

    /** {@return whether or not the container is the root container in an application.} */
    default boolean isApplicationContainer() {
        return application().container().equals(this);
    }

    /**
     * Returns whether or not an extension of the specified type is used by the container.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if this container uses an extension of the specified type, otherwise {@code false}
     */
    boolean isUsed(Class<? extends Extension> extensionType);

    // Altsaa hvor brugbar er denne... Ved man
    <T extends ExtensionMirror<?>> Optional<T> tryUse(Class<T> extensionMirrorType); // maybe just find? find

    /**
     * Returns an mirror for the specified extension type if it is in use. If the container does not use the specified
     * extension type a {@link NoSuchElementException} is thrown.
     * 
     * @param <T>
     *            the type of mirror
     * @param mirrorType
     *            the type of extension mirror to return
     * @return an extension mirror of the specified type
     * @see ContainerConfiguration#use(Class)
     * @see #tryUse(Class)
     * @throws NoSuchElementException
     *             if the mirror's extension is not used by the container
     */
    default <T extends ExtensionMirror<?>> T use(Class<T> mirrorType) {
        return tryUse(mirrorType).orElseThrow();
    }

    public static ContainerMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return BaseMirror.of(assembly, wirelets).container();
    }
}

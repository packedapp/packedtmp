package app.packed.container;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.component.Assembly;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;

/**
 * A mirror of a container (component).
 * <p>
 * An instance of this class is typically opb
 */
public interface ContainerMirror extends ComponentMirror {

    /** {@return a set of all extensions that are used by the container.} */
    Set<ExtensionMirror<?>> extensions();

    <T extends ExtensionMirror<?>> Optional<T> findExtension(Class<T> extensionMirrorType); // maybe just find

    /**
     * Returns whether or not an extension of the specified type is used by the container.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if this container uses an extension of the specified type, otherwise {@code false}
     */
    boolean isExtensionUsed(Class<? extends Extension> extensionType);

    /**
     * Returns an mirror for the specified extension type if it is in use. If the container does not use the specified
     * extension type a {@link NoSuchElementException} is thrown.
     * 
     * @param <T>
     *            the type of mirror
     * @param mirrorType
     *            the type of extension mirror to return
     * @return an extension mirror of the specified type
     * @see BaseContainerConfiguration#use(Class)
     * @see #findExtension(Class)
     * @throws NoSuchElementException
     *             if the mirror's extension is not used by the container
     */
    default <T extends ExtensionMirror<?>> T useExtension(Class<T> mirrorType) {
        return findExtension(mirrorType).orElseThrow();
    }

    public static ContainerMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return ApplicationMirror.of(assembly, wirelets);
    }
}

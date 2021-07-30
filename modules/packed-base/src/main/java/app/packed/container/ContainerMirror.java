package app.packed.container;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.component.ComponentMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;

/**
 * A mirror of a container (component).
 * <p>
 * An instance of this class is typically opb
 */
// Tror vi skal have en liste af banned extensions.
// Maaske baade dem inheriter, og dem vi ikke inheriter
public non-sealed interface ContainerMirror extends ComponentMirror {

    /** {@return a set mirrors for all extension that are in use by the container.} */
    Set<ExtensionMirror<?>> extensions();

    /** {@return a set of the extension types that are in use by the container.} */
    Set<Class<? extends Extension>> extensionsTypes();

    /**
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the mirror type
     * @return a mirror of the specified type, or empty if mirror of the requested type exists
     */
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
     * @param extensionMirrorType
     *            the type of extension mirror to return
     * @return an extension mirror of the specified type
     * @see ContainerConfiguration#use(Class)
     * @see #findExtension(Class)
     * @throws NoSuchElementException
     *             if the extension the mirror is a part of is not in use by the container
     */
    default <T extends ExtensionMirror<?>> T useExtension(Class<T> extensionMirrorType) {
        return findExtension(extensionMirrorType).orElseThrow();
    }

    public static ContainerMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return ApplicationMirror.of(assembly, wirelets).container();
    }
}

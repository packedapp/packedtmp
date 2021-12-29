package app.packed.container;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.component.ComponentMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.InternalExtensionException;

/**
 * A mirror of a container (component).
 * <p>
 * An instance of this class is typically obtained from an {@link ApplicationMirror}.
 */
public non-sealed interface ContainerMirror extends ComponentMirror {

    /** {@return a {@link Set} view of every extension that has been used in the container.} */
    Set<ExtensionMirror> extensions(); // return Map<Type, Mirror> instead???

    /** {@return a {@link Set} view of every extension type that has been used in the container.} */
    Set<Class<? extends Extension<?>>> extensionTypes();

    /**
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the mirror type
     * @return a mirror of the specified type, or empty if the extension the mirror represents is not used in the container
     */
    <T extends ExtensionMirror> Optional<T> findExtension(Class<T> extensionMirrorType);

    /**
     * Returns whether or not an extension of the specified type is in use by the container.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if the container uses an extension of the specified type, otherwise {@code false}
     * @see ContainerConfiguration#isExtensionUsed(Class)
     */
    boolean isExtensionUsed(Class<? extends Extension<?>> extensionType);

    /**
     * Returns an mirror of the specified type if the container is using the extension the mirror is a part of. Or throws
     * {@link NoSuchElementException} if the container does not use the specified extension type.
     * 
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the type of mirror to return
     * @return a mirror of the specified type
     * @see ContainerConfiguration#use(Class)
     * @see #findExtension(Class)
     * @throws NoSuchElementException
     *             if the extension the mirror is a part of is not in use by the container
     * @throws InternalExtensionException
     *             if the specified mirror class is not annotated with {@link ExtensionMember}.
     */
    default <T extends ExtensionMirror> T useExtension(Class<T> extensionMirrorType) {
        return findExtension(extensionMirrorType).orElseThrow();
    }
}
// TODO
// * List of banned extensions? Maaske baade dem inheriter, og dem vi ikke inheriter

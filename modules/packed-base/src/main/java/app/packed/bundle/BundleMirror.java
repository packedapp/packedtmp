package app.packed.bundle;

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
 * An instance of this class is typically opb
 */
// Tror vi skal have en liste af banned extensions.
// Maaske baade dem inheriter, og dem vi ikke inheriter
public non-sealed interface BundleMirror extends ComponentMirror {

    /** {@return a {@link Set} view of mirrors for every extension that is used by the container.} */
    Set<ExtensionMirror> extensions();

    /** {@return a {@link Set} view of every extension type used by the container.} */
    Set<Class<? extends Extension>> extensionsTypes();

    /**
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the mirror type
     * @return a mirror of the specified type, or empty if mirror of the requested type exists
     */
    <T extends ExtensionMirror> Optional<T> findExtension(Class<T> extensionMirrorType); // maybe just find

    /**
     * Returns whether or not an extension of the specified type is used by the container.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if this container uses an extension of the specified type, otherwise {@code false}
     * @see BundleConfiguration#isExtensionUsed(Class)
     */
    boolean isExtensionUsed(Class<? extends Extension> extensionType);

    /**
     * Returns an mirror of the specified type if the container is using the extension the mirror is a part of. Or throws
     * {@link NoSuchElementException} if the container does not use the specified extension type.
     * 
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the type of mirror to return
     * @return a mirror of the specified type
     * @see BundleConfiguration#use(Class)
     * @see #findExtension(Class)
     * @throws NoSuchElementException
     *             if the extension the mirror is a part of is not in use by the container
     * @throws InternalExtensionException
     *             if the specified mirror class is not annotated with {@link ExtensionMember}.
     */
    default <T extends ExtensionMirror> T useExtension(Class<T> extensionMirrorType) {
        return findExtension(extensionMirrorType).orElseThrow();
    }

    public static BundleMirror of(Bundle<?> assembly, Wirelet... wirelets) {
        return ApplicationMirror.of(assembly, wirelets).container();
    }
}

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
 * A mirror of a bundle (component).
 * <p>
 * An instance of this class is typically opb
 */
// Tror vi skal have en liste af banned extensions, Maaske baade dem inheriter, og dem vi ikke inheriter
public non-sealed interface BundleMirror extends ComponentMirror {

    /** {@return a {@link Set} view of mirrors for every extension that is in use.} */
    Set<ExtensionMirror> extensions();

    /** {@return a {@link Set} view of every extension type that is in use.} */
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
     * Returns whether or not an extension of the specified type is in use by the bundle.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if the bundle uses an extension of the specified type, otherwise {@code false}
     * @see BundleConfiguration#isExtensionUsed(Class)
     */
    boolean isExtensionUsed(Class<? extends Extension> extensionType);

    /**
     * Returns the bundle class that was used to build the bundle.
     * <p>
     * (alternative, defining bundle type) Returns the bundle type which is the class used for building the bundle
     * <p>
     * If composer returns {@code Bundle.class} except for example ServiceExtension.transformExports
     * 
     * @return the bundle type
     */
    Class<? extends BundleAssembly > type();

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

    public static BundleMirror of(BundleAssembly  assembly, Wirelet... wirelets) {
        return ApplicationMirror.of(assembly, wirelets).bundle();
    }
}

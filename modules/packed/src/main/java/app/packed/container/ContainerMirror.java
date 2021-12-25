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
import app.packed.inject.service.ServiceLocator;

/**
 * A mirror of a container (component).
 * <p>
 * An instance of this class is typically opb
 */
// Tror vi skal have en liste af banned extensions, Maaske baade dem inheriter, og dem vi ikke inheriter
public non-sealed interface ContainerMirror extends ComponentMirror {

    // Set<BeanMirror> beans(), or maybe just BeanExtensionMirror

    /**
     * Returns the assembly class from which the container was build.
     * <p>
     * Returns {@code container.class} if the container outside an assembly, for example, via
     * {@link ServiceLocator#of(ComposerAction)}
     * 
     * @return the assembly class from which the container was build
     */
    Class<? extends Assembly> assemblyType();

    /** {@return a {@link Set} view of every extension that is used when building the container.} */
    // This may differ from runtime usage...
    Set<ExtensionMirror> extensions();

    /** {@return a {@link Set} view of every extension type that is used when building the container.} */
    Set<Class<? extends Extension>> extensionTypes();

    /**
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the mirror type
     * @return a mirror of the specified type, or empty if mirror of the requested type exists
     */
    <T extends ExtensionMirror> Optional<T> findExtension(Class<T> extensionMirrorType); // maybe just find

    /**
     * Returns whether or not an extension of the specified type is in use by the container.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return {@code true} if the container uses an extension of the specified type, otherwise {@code false}
     * @see ContainerConfiguration#isExtensionUsed(Class)
     */
    // maybe skip it, if we have runtimeExtensions()
    // because we would also need isExtensionUsedAtRuntime
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

    public static ContainerMirror of(Assembly assembly, Wirelet... wirelets) {
        return ApplicationMirror.of(assembly, wirelets).container();
    }
}

package app.packed.container;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.application.ComponentMirror;
import app.packed.bean.BeanMirror;
import packed.internal.container.ContainerSetup.BuildTimeContainerMirror;

/**
 * A mirror of a container.
 * <p>
 * Instances of this class is typically via {@link ApplicationMirror}.
 */
public sealed interface ContainerMirror extends ComponentMirror permits BuildTimeContainerMirror {

    /** {@return a {@link Collection} view of all the beans defined in the container.} */
    Collection<BeanMirror> beans();

    /** {@return a {@link Set} view of every extension that have been used in the container.} */
    // return Map<Class<Ext>, Mirror> instead???
    // Altsaa hvad vil bruge metoden til???
    // Kan ikke lige umiddelbart se nogle use cases
    // Maaske bare fjerne den
    Set<ExtensionMirror<?>> extensions();

    /** {@return the parent container of this container. Or empty if the root container.} */
    Optional<ContainerMirror> parent();

    /** {@return a {@link Set} view of every extension type that have been used in the container.} */
    Set<Class<? extends Extension<?>>> extensionTypes();

    /**
     * <p>
     * If you know for certain that extension is used in the container you can use {@link #useExtension(Class)} instead.
     * 
     * @param <T>
     *            the type of mirror
     * @param extensionMirrorType
     *            the mirror type
     * @return a mirror of the specified type, or empty if the extension the mirror represents is not used in the container
     */
    <T extends ExtensionMirror<?>> Optional<T> findExtension(Class<T> extensionMirrorType);

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
     * @see ApplicationMirror#useExtension(Class)
     * @see #findExtension(Class)
     * @throws NoSuchElementException
     *             if the mirror's extension is not in use by the container
     */
    default <T extends ExtensionMirror<?>> T useExtension(Class<T> extensionMirrorType) {
        return findExtension(extensionMirrorType).orElseThrow();
    }
}

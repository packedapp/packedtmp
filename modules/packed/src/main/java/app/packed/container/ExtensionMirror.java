package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import app.packed.base.Nullable;
import app.packed.mirror.Mirror;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.PackedExtensionTree;

/**
 * Provides generic information about an extension used by a {@link #container}.
 * <p>
 * This class can be extended by an extension to provide more detailed information about itself. For example,
 * {@link app.packed.inject.service.ServiceExtension} extends this class via
 * {@link app.packed.inject.service.ServiceExtensionMirror}.
 * <p>
 * Extension mirror instances are typically obtained in one of the following ways:
 * <ul>
 * <li>By calling methods on other mirrors, for example, {@link ContainerMirror#extensions()} or
 * {@link ContainerMirror#findExtension(Class)}.</li>
 * </ul>
 * <p>
 * 
 * @see Extension#newExtensionMirror()
 * @param <E>
 *            The type of extension this extension mirror is a part of. The extension mirror must be located in the same
 *            module as the extension itself.
 */
public class ExtensionMirror<E extends Extension<E>> implements Mirror {

    /**
     * The extensions that are being mirrored. Is initially null but populated via {@link #initialize(PackedExtensionTree)}
     */
    @Nullable
    private PackedExtensionTree<E> extensions;

    /**
     * Create a new extension mirror.
     * <p>
     * Subclasses should have a single constructor with package access.
     */
    protected ExtensionMirror() {}

    protected final boolean allAnyMatch(Predicate<? super E> predicate) {
        return allStream().anyMatch(predicate);
    }
    
    protected final Stream<E> allStream() {
        return tree().stream();
    }
    
    protected final int allSumInt(ToIntFunction<? super E> mapper) {
        return tree().sumInt(mapper);
    }
    
    /** {@return a descriptor for the extension this mirror is a part of.} */
    public final ExtensionDescriptor extensionDescriptor() {
        return extensions().extension().model;
    }

    // All methods are named extension* instead of * because subclasses might want to use method names such as descriptor,
    // name, type

    /** {@return the full name of the extension.} */
    public final String extensionFullName() {
        return extensionDescriptor().fullName();
    }

    /** {@return the name of the extension.} */
    public final String extensionName() {
        return extensionDescriptor().name();
    }

    /**
     * {@return the mirrored extension's internal configuration.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror
     */
    private PackedExtensionTree<E> extensions() {
        PackedExtensionTree<E> e = extensions;
        if (e == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to invoke Extension#mirrorInitialize.");
        }
        return e;
    }

    /** {@return the type of extension this mirror is a part of.} */
    public final Class<? extends Extension<?>> extensionType() {
        return extensions().extension().extensionType;
    }

    /**
     * Invoked by {@link packed.internal.container.ExtensionMirrorModel#initialize(ExtensionMirror, ExtensionSetup)} to set the internal configuration of the extension.
     * 
     * @param extension
     *            the internal configuration of the extension to mirror
     */
    final void initialize(PackedExtensionTree<E> extensions) {
        if (this.extensions != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.extensions = requireNonNull(extensions);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return extensionType().getCanonicalName();
    }

    protected ExtensionTree<E> tree() {
        extensions();
        return extensions;
    }
}

//
///** {@inheritDoc} */
//@Override
//public final boolean equals(Object other) {
//  // Use case for equals on mirrors are
//  // FooBean.getExtension().equals(OtherBean.getExtension())...
//  // Hmm virker ikke super godt med trees...
//  // Altsaa med mindre det altid inkludere alle sub extensions
//
//  // Normally there should be no reason for subclasses to override this method...
//  // If we find a valid use case we can always remove final
//
//  // Check other.getType()==getType()????
//
//  // TODO if we have local extensions, we cannot just rely on extension=extension
//  //
//  return this == other || other instanceof ExtensionMirror<?> m && extension() == m.extension();
//}
///** {@inheritDoc} */
//@Override
//public final int hashCode() {
//  return extension().hashCode();
//}
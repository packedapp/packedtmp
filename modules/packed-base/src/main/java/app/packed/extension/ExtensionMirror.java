package app.packed.extension;

import java.util.NoSuchElementException;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.container.Bundle;
import app.packed.container.BundleMirror;
import app.packed.container.Wirelet;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceExtensionMirror;
import packed.internal.container.ExtensionSetup;

/**
 * Provides generic information about an extension used by a {@link #container}.
 * <p>
 * This class can be extended by an extension to provide more detailed information about itself. For example,
 * {@link app.packed.service.ServiceExtension} extends this class via {@link app.packed.service.ServiceExtensionMirror}.
 * <p>
 * Extension mirror instances are typically obtained in one of the following ways:
 * <ul>
 * <li>By calling methods on other mirrors, for example, {@link BundleMirror#extensions()} or
 * {@link BundleMirror#findExtension(Class)}.</li>
 * <li>By calling {@link Extension#mirror()}, for example, {@link ServiceExtension#mirror()}.</li>
 * <li>By calling a factory method on the mirror class, for example,
 * {@link ServiceExtensionMirror#use(Bundle, app.packed.container.Wirelet...)}.</li>
 * </ul>
 * <p>
 * NOTE: Subclasses of this class:
 * <ul>
 * <li>Must be annotated with {@link ExtensionMember} indicating what extension that is being mirrored.</li>
 * <li>Must be located in the same module as the extension itself (iff the extension is defined in a module).</li>
 * <li>Must override {@link Extension#mirror()} in order to provide a mirror instance to the framework.</li>
 * <li>May provide factory methods, similar to {@link ServiceExtensionMirror#of(Bundle, Wirelet...)}.
 * </ul>
 */
public class ExtensionMirror {

    /**
     * The internal configuration of the extension we are mirrored. Is initially null but populated via
     * {@link #initialize(ExtensionSetup)} which must be called by extension developers via
     * {@link Extension#mirrorInitialize(ExtensionMirror)}.
     */
    @Nullable
    private ExtensionSetup extension;

    /**
     * Create a new extension mirror.
     * <p>
     * Subclasses should have a single package-protected constructor. Instantiated by overriding {@link Extension#mirror()}.
     */
    protected ExtensionMirror() {}

    /** {@return the container the extension is used in.} */
    public final BundleMirror container() {
        return setup().container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        // Use case for equals on mirrors are
        // FooBean.getExtension().equals(OtherBean.getExtension())...

        // Normally there should be no reason for subclasses to override this method...
        // If we find a valid use case we can always remove final

        // Check other.getType()==getType()????
        return this == other || other instanceof ExtensionMirror m && setup() == m.setup();
    }

    /** {@return a descriptor for the extension this mirror is a part of.} */
    public final ExtensionDescriptor extensionDescriptor() { // extensionDescriptor() instead of descriptor() because subclasses might want to use descriptor()
        return ExtensionDescriptor.of(extensionType());
    }

    /** {@return the type of extension this mirror is a part of.} */
    public final Class<? extends Extension> extensionType() { // extensionType() instead of type() because subclasses might want to use type()
        return setup().extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return setup().hashCode();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     * 
     * @param extension
     *            the internal configuration of the extension to mirror
     */
    final void initialize(ExtensionSetup extension) {
        if (this.extension != null) {
            throw new IllegalStateException("The specified mirror has already been initialized.");
        }
        this.extension = extension;
    }

    /**
     * {@return the mirrored extension's internal configuration.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror, or the implementation of the extension forgot to call
     *             {@link Extension#mirrorInitialize(ExtensionMirror)} from {@link Extension#mirror()}.
     */
    private ExtensionSetup setup() {
        ExtensionSetup e = extension;
        if (e == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to invoke Extension#mirrorInitialize.");
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return extensionType().getCanonicalName();
    }

    /**
     * Builds an application, and returns a mirror for an extension in the root container if it is present. Otherwise
     * {@link Optional#empty()}.
     * 
     * @param <E>
     *            the type of extension mirror
     * @param mirrorType
     *            the type of extension mirror to return
     * @param assembly
     *            the assembly
     * @param wirelets
     *            optional wirelets
     * @return stuff
     * @see BundleMirror#findExtension(Class)
     * @see #of(Class, Bundle, Wirelet...)
     */
    public static <E extends ExtensionMirror> Optional<E> find(Class<E> mirrorType, Bundle<?> assembly, Wirelet... wirelets) {
        return BundleMirror.of(assembly, wirelets).findExtension(mirrorType);
    }

    /**
     * Builds an application. Throws {@link NoSuchElementException} if the root container does not use the mirror's
     * extension type.
     * 
     * @param <E>
     *            the type of extension mirror
     * @param mirrorType
     *            the type of extension mirror to return
     * @param assembly
     *            the assembly
     * @param wirelets
     *            optional wirelets
     * @return stuff
     * @see BundleMirror#useExtension(Class)
     * @see #find(Class, Bundle, Wirelet...)
     * @throws NoSuchElementException
     *             if the root container in the mirrored application does not use the extension that the specified mirror is
     *             a part of
     */
    public static <E extends ExtensionMirror> E of(Class<E> extensionMirrorType, Bundle<?> assembly, Wirelet... wirelets) {
        return BundleMirror.of(assembly, wirelets).useExtension(extensionMirrorType);
    }
}

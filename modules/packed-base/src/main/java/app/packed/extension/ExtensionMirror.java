package app.packed.extension;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import packed.internal.container.ExtensionSetup;

/**
 * A mirror for an extension.
 * <p>
 * This class can be overridden to provide a specialized extension mirror. For example, {@link app.packed.service.ServiceExtension}
 * provides {@link app.packed.service.ServiceExtensionMirror}.
 * <p>
 * Instances of this mirror are typically obtained in one of the following ways:
 * <ul>
 * <li>Directly Paa et mirror selv</li>
 * <li>via en overskreven {@link Extension#mirror()} metode</li>
 * <li>Or indirectly via invoking methods on other mirrors, for example, {@link ContainerMirror#extensions()} via
 * ContainerMirror.extensions();</li>
 * </ul>
 * <p>
 * NOTE: Extensions that implement there own specialized mirror <strong>must</strong> override {@link Extension#mirror()} to return an instance of the
 * specialized mirror.
 */
public non-sealed class ExtensionMirror<E extends Extension> implements ExtensionMember<E> {

    /** The extension that is being mirrored. Is initially null but populated via {@link #populate(ExtensionSetup)}. */
    @Nullable
    private ExtensionSetup extension;

    /**
     * Create a new extension mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    protected ExtensionMirror() {}

    /** {@return the container the extension is used in.} */
    public final ContainerMirror container() {
        return extension().container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        // Use case for equals on mirrors are
        // FooBean.getExtension().equals(OtherBean.getExtension())...

        // Normally there should be no reason for subclasses to override this method...
        // If we find a valid use case we can always remove final

        // Check other.getType()==getType()????
        return this == other || other instanceof ExtensionMirror<?> m && extension() == m.extension();
    }

    /**
     * {@return the actual extension.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror, or the extension developer forgot to call
     *             {@link Extension#mirrorPopulate(ExtensionMirror)}.
     */
    private ExtensionSetup extension() {
        ExtensionSetup e = extension;
        if (e == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to invoke Extension#mirrorPopulate");
        }
        return e;
    }

    /** {@return a descriptor of the extension.} */
    public final ExtensionDescriptor extensionDescriptor() { // extensionDescriptor() instead of descriptor() because subclasses might want to use descriptor()
        return ExtensionDescriptor.of(extensionType());
    }

    /** {@return the type of extension that is mirrored.} */
    public final Class<? extends Extension> extensionType() { // extensionType() instead of type() because subclasses might want to use type()
        return extension().extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return extension().hashCode();
    }

    /**
     * Invoked by {@link Extension#mirrorPopulate(ExtensionMirror)} to set the extension we are mirroring.
     * 
     * @param extension
     *            the extension to mirror
     */
    final void populate(ExtensionSetup extension) {
        if (this.extension != null) {
            throw new IllegalStateException("The specified mirror has already been populated.");
        }
        this.extension = extension;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return extensionType().getCanonicalName();
    }
}

class ZandboxEM {

    // installed by extensions???
    final Set<ComponentMirror> installed() {
        throw new UnsupportedOperationException();
    }

}

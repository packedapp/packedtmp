package app.packed.extension;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceExtensionMirror;
import packed.internal.container.ExtensionSetup;

/**
 * A mirror for an extension.
 * <p>
 * This class can be overridden to provide a specialized mirror for an extension. For example,
 * {@link app.packed.service.ServiceExtension} provides {@link app.packed.service.ServiceExtensionMirror}.
 * <p>
 * Instances of this mirror are typically obtained in one of the following ways:
 * <ul>
 * <li>By calling methods on other mirrors, for example, {@link ContainerMirror#extensions()} or
 * {@link ContainerMirror#findExtension(Class)}.</li>
 * <li>via en overskreven {@link Extension#mirror()} metode, for example, {@link ServiceExtension#mirror()}.</li>
 * <li>By directly looking a specialized mirror up, for example,
 * {@link ServiceExtensionMirror#of(app.packed.component.Assembly, app.packed.component.Wirelet...)}.</li>
 * </ul>
 * <p>
 * NOTE: Extensions that extends this class:
 * <ul>
 * <li>Must be annotated with {@link ExtensionMember} to indicate what extension they are a part of.</li>
 * <li>Must be located in the same module as the extension itself.</li>
 * <li>Must override {@link Extension#mirror()} in order to provide a mirror instance to Packed.</li>
 * <li>Must call {@link Extension#mirrorInitialize(ExtensionMirror)} on the mirror instance before returning from
 * the.</li>
 * <li>Must support being populated at any point. Including immediately after the extension is created. After
 * {@link Extension#onNew()} returns.</li>
 * <li>May provide a shortcut method, similar to ServiceExtension.of
 * <li>call populate.</li>
 * </ul>
 */
public class ExtensionMirror<E extends Extension> {

    /** The extension that is being mirrored. Is initially null but populated via {@link #initialize(ExtensionSetup)}. */
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
     *             {@link Extension#mirrorInitialize(ExtensionMirror)}.
     */
    private ExtensionSetup extension() {
        ExtensionSetup e = extension;
        if (e == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to invoke Extension#mirrorPopulate");
        }
        return e;
    }

    /** {@return a descriptor for the extension.} */
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
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the extension we are mirroring.
     * 
     * @param extension
     *            the extension to mirror
     */
    final void initialize(ExtensionSetup extension) {
        if (this.extension != null) {
            throw new IllegalStateException("The specified mirror has already been initialized.");
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

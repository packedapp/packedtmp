package app.packed.extension;

import static java.util.Objects.requireNonNull;

import app.packed.build.BuildCodeSourceMirror;
import app.packed.container.ContainerMirror;
import internal.app.packed.extension.PackedExtensionHandle;

/**
 * A mirror for an extension used in a container.
 * <p>
 * Extension mirror instances are typically obtained via calls to
 * {@link ContainerMirror#isExtensionUsed(Class)}.
 *
 * @see ContainerMirror#isExtensionUsed(Class)
 */
public final class ExtensionMirror implements BuildCodeSourceMirror {

    final PackedExtensionHandle<?> handle;

    /**
     * Create a new extension mirror.
     */
    ExtensionMirror(ExtensionHandle<?> handle) {
        this.handle = (PackedExtensionHandle<?>) requireNonNull(handle);
    }

    /** {@return the container this extension is part of.} */
    public ContainerMirror container() {
        return handle.extension().container.mirror();
    }

    /** {@return the class of the extension.} */
    public Class<? extends Extension<?>> extensionClass() {
        return handle.extension().extensionType;
    }

    /** {@return a descriptor for the extension this mirror is a part of.} */
    public ExtensionDescriptor extensionDescriptor() {
        return ExtensionDescriptor.of(extensionClass());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Mirror for " + extensionDescriptor().type().getCanonicalName();
    }
}

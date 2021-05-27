package app.packed.container;

import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceExtensionMirror;
import packed.internal.container.ExtensionSetup;

/**
 * A generic mirror for an extension.
 * <p>
 * This class can be overridden to provide a specialized mirror for an extension . For example, {@link ServiceExtension}
 * provides the specialized mirror {@link ServiceExtensionMirror}. Extensions that provide a specialized mirror must
 * override {@link Extension#mirror()} to return an instance of the specialized mirror.
 */
public class ExtensionMirror<E extends Extension> {

    /** The extension all calls on this interface delegates to. */
    @Nullable
    private ExtensionSetup extension;

    /**
     * Create a new extension mirror. Subclasses should have a single package-protected constructor. That are only invoked
     * via an overridden {@link Extension#mirror()}.
     */
    protected ExtensionMirror() {}

    /** {@return the container this extension is used in.} */
    public final ContainerMirror container() {
        return extension().container.mirror();
    }

    /** {@return a static extension descriptor.} */
    public final ExtensionDescriptor descriptor() {
        return ExtensionDescriptor.of(type());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        return other instanceof ExtensionMirror<?> m && extension() == m.extension();
    }

    private ExtensionSetup extension() {
        ExtensionSetup e = extension;
        if (e == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to populate it");
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return extension().hashCode();
    }

    public final Set<ComponentMirror> installed() {
        throw new UnsupportedOperationException();
    }

    void populate(ExtensionSetup es) {
        if (this.extension != null) {
            throw new IllegalStateException("The specified mirror has already been populated.");
        }
        this.extension = es;

    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return type().getCanonicalName();
    }

    /** {@return the type of extension that is mirrored.} */
    public final Class<? extends Extension> type() {
        return extension().extensionType;
    }
}

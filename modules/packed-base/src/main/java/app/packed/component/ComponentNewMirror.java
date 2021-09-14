package app.packed.component;

import app.packed.application.ApplicationMirror;
import app.packed.base.Nullable;
import app.packed.bean.BeanNewMirror;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleMirror;
import app.packed.bundle.Wirelet;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.extension.InternalExtensionException;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceExtensionMirror;
import packed.internal.bundle.ExtensionSetup;
import packed.internal.component.ComponentSetup;

/**
 * A mirror of an extension that is in use by a {@link #container}.
 * <p>
 * This class can be overridden to provide a specialized mirror for an extension. For example,
 * {@link app.packed.service.ServiceExtension} provides {@link app.packed.service.ServiceExtensionMirror}.
 * <p>
 * Extensions mirrors are typically obtained in one of the following ways:
 * <ul>
 * <li>By calling methods on other mirrors, for example, {@link BundleMirror#extensions()} or
 * {@link BundleMirror#findExtension(Class)}.</li>
 * <li>Exposed directly on an extension, for example, {@link ServiceExtension#mirror()}.</li>
 * <li>By calling a factory method on the mirror itself, for example,
 * {@link ServiceExtensionMirror#use(Bundle, app.packed.bundle.Wirelet...)}.</li>
 * </ul>
 * <p>
 * NOTE: If overriding this class, subclasses:
 * <ul>
 * <li>Must be annotated with {@link ExtensionMember} to indicate what extension they are a part of.</li>
 * <li>Must override {@link Extension#mirror()} in order to provide a mirror instance to the runtime.</li>
 * <li>Must be located in the same module as the extension itself.</li>
 * <li>May provide factory methods, similar to {@link ServiceExtensionMirror#use(Bundle, Wirelet...)}.
 * </ul>
 */
public sealed class ComponentNewMirror permits BeanNewMirror {

    /**
     * The internal configuration of the extension we are mirrored. Is initially null but populated via
     * {@link #initialize(ExtensionSetup)} which must be called by extension developers via
     * {@link Extension#mirrorInitialize(ComponentNewMirror)}.
     */
    @Nullable
    private ComponentSetup component;

    /**
     * Create a new component mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    protected ComponentNewMirror() {}

    /** {@return the application this component is a part of.} */
    public ApplicationMirror application() {
        return component().application.mirror();
    }

    /**
     * {@return the internal configuration of the extension.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror, or the extension developer forgot to call
     *             {@link Extension#mirrorInitialize(ComponentNewMirror)}.
     */
    private ComponentSetup component() {
        ComponentSetup e = component;
        if (e == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to invoke Extension#mirrorInitialize.");
        }
        return e;
    }

    /** {@return the container the extension is used in.} */
    public final BundleMirror container() {
        return component().container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        // Use case for equals on mirrors are
        // FooBean.getExtension().equals(OtherBean.getExtension())...

        // Normally there should be no reason for subclasses to override this method...
        // If we find a valid use case we can always remove final

        // Check other.getType()==getType()????
        return this == other || other instanceof ComponentNewMirror m && component() == m.component();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return component().hashCode();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ComponentNewMirror)} to set the internal configuration of the extension.
     * 
     * @param extension
     *            the internal configuration of the extension to mirror
     */
    final void initialize(ComponentSetup component) {
        if (this.component != null) {
            throw new IllegalStateException("The specified mirror has already been initialized.");
        }
        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return component().toString();
    }
}

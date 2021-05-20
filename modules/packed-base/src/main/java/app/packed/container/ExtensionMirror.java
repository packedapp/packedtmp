package app.packed.container;

import java.util.Set;

import app.packed.component.ComponentMirror;
import packed.internal.container.AbstractExtensionMirrorContext;

/**
 * A mirror of an extension.
 * <p>
 * This class can be overridden by extension to provide more details about a particular extension.
 */
// 2 Design maader...
// Enten en ExtensionMirror i constructeren.. return new BuildTimeServiceExtensionMirror(mirror());
// eller en Extension.populate(ExtensionMirror em)
public class ExtensionMirror<E extends Extension> {

    @SuppressWarnings("exports")
    public AbstractExtensionMirrorContext context;

    /** {@return the container this extension is used in.} */
    public final ContainerMirror container() {
        return context().container();
    }

    private AbstractExtensionMirrorContext context() {
        return context;
    }

    /** {@return a descriptor of the extension.} */
    public final ExtensionDescriptor descriptor() {
        return ExtensionDescriptor.of(type());
    }

    @Override
    public boolean equals(Object other) {
        // I think we just compare extension instance
        return context().equalsTo(other);
    }

    @Override
    public int hashCode() {
        return context().hashCode();
    }

    public final Set<ComponentMirror> installed() {
        return context().installed();
    }

    @Override
    public String toString() {
        return type().getCanonicalName();
    }

    /** {@return the type of extension that is mirrored.} */
    public final Class<? extends Extension> type() {
        return context().type();
    }
}

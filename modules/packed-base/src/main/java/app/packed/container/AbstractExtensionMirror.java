package app.packed.container;

import java.util.Set;

import app.packed.component.ComponentMirror;
import packed.internal.container.AbstractExtensionMirrorContext;

public abstract class AbstractExtensionMirror<E extends Extension> implements ExtensionMirror<E> {

    @SuppressWarnings("exports")
    public AbstractExtensionMirrorContext context;

    private AbstractExtensionMirrorContext context() {
        return context;
    }

    @Override
    public int hashCode() {
        return context().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return context.equalsTo(other);
    }

    @Override
    public final ExtensionDescriptor descriptor() {
        return ExtensionMirror.super.descriptor();
    }

    @Override
    public final Set<ComponentMirror> installed() {
        return context().installed();
    }

    @Override
    public final Class<? extends Extension> type() {
        return context().type();
    }

    @Override
    public String toString() {
        return type().getCanonicalName();
    }
}

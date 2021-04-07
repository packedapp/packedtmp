package app.packed.hooks.v2;

import java.util.Optional;

import app.packed.base.MetaAnnotatedElement;
import app.packed.container.Extension;

// What we got so far.
// This all for hooks that relates to some kind of Java language concept
public abstract class AbstractHookBootstrap implements MetaAnnotatedElement {

    /**
     * If the requesting party is part of an extension, returns the extension. Otherwise returns empty.
     * <p>
     * Any extension returned by this method is guaranteed to have {@link ServiceHook#extension()} as a (direct) dependency.
     * 
     * @return the extension using the hook, or empty if user code
     * @see #$failForOtherExtensions()
     */
    public final Optional<Class<? extends Extension>> extension() {
        throw new UnsupportedOperationException();
    }

    /** {@return the module of the user}. */
    public final Module module() {
        throw new UnsupportedOperationException();
    }
}

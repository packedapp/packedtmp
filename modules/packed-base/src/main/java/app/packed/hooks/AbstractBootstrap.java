package app.packed.hooks;

import java.util.Optional;

import app.packed.extension.Extension;
import app.packed.hooks.v2.ServiceHook;

// What we got so far.
// This all for hooks that relates to some kind of Java language concept
public abstract class AbstractBootstrap {

    /**
     * Replaces this bootstrap with the specified instance at build-time (and run-time).
     * 
     * @param instance
     *            the instance to replace this bootstrap with
     * 
     * @throws IllegalStateException
     *             if {@link #disable()} has already been called o
     * 
     */
    public final void buildWith(Object instance) {
        throw new UnsupportedOperationException();
    }

    public final void buildWithPrototype(Class<?> implementation) {
        buildWithPrototype(implementation, this);
    }
    
    // Kan egentlig gemme den i en ClassValueMap.. bare med attachment.getClass()
    // Vi finder bare constructoren her...
    // Resten af scanningen laver vi senere...
    // Attachment er kun tilgaengelig i constructoren...
    // extensionen er ogsaa tilgaengelig...
    public final void buildWithPrototype(Class<?> implementation, Object attachment) {
        throw new UnsupportedOperationException();
    }

    /** Disables any further processing of the hook. */
    // Can service Hook ogsaa goere dette????
    public final void disable() {
        throw new UnsupportedOperationException();
    }

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

    /** {@return the module of the target}. */
    public final Module module() {
        throw new UnsupportedOperationException();
    }
}

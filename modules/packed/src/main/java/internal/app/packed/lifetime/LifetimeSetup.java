package internal.app.packed.lifetime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import app.packed.base.Nullable;
import app.packed.lifetime.LifetimeMirror;
import internal.app.packed.lifetime.pool.LifetimePoolSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The internal configuration of a Lifetime. */

// Eager, Lazy, Many

// Bean.Eager -> Created together with the container
// Bean.lazy -> A single instance is created when needed
// Bean.Many -> Has its own lifetime

// Container.Eager -> Don't know if this makes sense for non-root?
// Container.Lazy
// Container.Many -> Create as many as you want to

//Application.Eager -> launch
//Application.Lazy -> launcher
//Application.Many -> image

public class LifetimeSetup {

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    private static final MethodHandle MH_LIFETIME_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), LifetimeMirror.class,
            "initialize", void.class, LifetimeSetup.class);

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    /** Any parent of this lifetime. The root lifetime always being identical to the application lifetime. */
    @Nullable
    public final LifetimeSetup parent;

    /** The application's constant pool. */
    public final LifetimePoolSetup pool = new LifetimePoolSetup();

    /**
     * Creates a new lifetime.
     * 
     * @param rootContainer
     *            the application's root container
     */
    public LifetimeSetup(@Nullable ContainerLifetimeSetup parent) {
        this.parent = parent;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    public LifetimeMirror mirror() {
        LifetimeMirror mirror = new LifetimeMirror();

        // Initialize LifetimeMirror by calling LifetimeMirror#initialize(LifetimeSetup)
        try {
            MH_LIFETIME_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }
}

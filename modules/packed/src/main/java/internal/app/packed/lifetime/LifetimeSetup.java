package internal.app.packed.lifetime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.framework.Nullable;
import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationTemplate;
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

public abstract sealed class LifetimeSetup permits ContainerLifetimeSetup, BeanLifetimeSetup {

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    private static final MethodHandle MH_LIFETIME_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), LifetimeMirror.class,
            "initialize", void.class, LifetimeSetup.class);

    public final List<OperationTemplate> lifetimes;

    /** Any parent of this lifetime. The root lifetime always being identical to the application lifetime. */
    @Nullable
    public final ContainerLifetimeSetup parent;

    /**
     * Creates a new lifetime.
     * 
     * @param parent
     *            a parent lifetime
     */
    LifetimeSetup(@Nullable ContainerLifetimeSetup parent, List<OperationTemplate> lifetimes) {
        this.parent = parent;
        this.lifetimes = lifetimes;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    public LifetimeMirror mirror() {
        LifetimeMirror mirror = mirror0();

        // Initialize LifetimeMirror by calling LifetimeMirror#initialize(LifetimeSetup)
        try {
            MH_LIFETIME_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    abstract LifetimeMirror mirror0();
}

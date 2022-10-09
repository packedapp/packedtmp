package internal.app.packed.lifetime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.lifetime.LifetimeMirror;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.lifetime.pool.LifetimePoolSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The internal configuration of a Lifetime. */
public class LifetimeSetup {

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    private static final MethodHandle MH_LIFETIME_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), LifetimeMirror.class,
            "initialize", void.class, LifetimeSetup.class);

    /** Any child lifetimes. */
    private List<LifetimeSetup> children;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    /** The root component of the lifetime. */
    public final ComponentSetup origin;

    // Der er jo som saadan ikke noget vi vejen for at vi har en DAG istedet for et trae...
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
    public LifetimeSetup(ComponentSetup origin, @Nullable LifetimeSetup parent) {
        this.origin = requireNonNull(origin);
        this.parent = parent;
    }

    public LifetimeSetup addChild(ComponentSetup component) {
        LifetimeSetup l = new LifetimeSetup(component, this);
        if (children == null) {
            children = new ArrayList<>(1);
        }
        children.add(l);
        return l;
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

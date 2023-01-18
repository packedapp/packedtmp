package internal.app.packed.lifetime;

import java.util.List;

import app.packed.framework.Nullable;
import app.packed.lifetime.LifetimeMirror;

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

public abstract sealed interface LifetimeSetup permits ContainerLifetimeSetup, BeanLifetimeSetup {
    
    List<FuseableOperation> lifetimes();
    
    @Nullable
    ContainerLifetimeSetup parent();

    /** {@return a mirror that can be exposed to end-users.} */
    LifetimeMirror mirror();
}

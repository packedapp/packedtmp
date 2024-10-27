package internal.app.packed.lifecycle.lifetime;

import java.util.List;

import app.packed.lifetime.LifetimeMirror;
import app.packed.util.Nullable;
import internal.app.packed.operation.OperationSetup;

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

public sealed interface LifetimeSetup permits ContainerLifetimeSetup, BeanLifetimeSetup {

    default List<OperationSetup> entryPoints() {
        throw new UnsupportedOperationException();
    }

    /** {@return a mirror that can be exposed to end-users.} */
    LifetimeMirror mirror();

    /** {@return any parent lifetime of this lifetime.} */
    @Nullable
    ContainerLifetimeSetup parent();

    Class<?> resultType();
}

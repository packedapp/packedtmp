package app.packed.bean.operation;

import app.packed.application.ApplicationMirror;
import app.packed.application.entrypoint.EntryPointExtensionMirror;
import app.packed.bean.mirror.BeanOperationMirror;
import app.packed.lifecycle.RunState;

/**
 * A mirror representing an operation that will be invoked due single entry point in an application. Instances of this
 * mirror are normally acquired by calling various methods on a {@link EntryPointExtensionMirror} instance.
 */
public final class BeanLifecycleOperationMirror extends BeanOperationMirror {

    /** {@return the state at which the operation will be invoked.} */
    public RunState state() {
        return RunState.RUNNING;
    }

    public static void main(ApplicationMirror am) {
        am.operations(BeanLifecycleOperationMirror.class).stream().filter(m -> m.state() == RunState.INITIALIZED).count();
    }
}

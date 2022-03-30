package app.packed.bean.operation.lifecycle;

import app.packed.application.ApplicationMirror;
import app.packed.application.entrypoint.EntryPointExtensionMirror;
import app.packed.bean.operation.OperationMirror;
import app.packed.lifecycle.RunState;

/**
 * A mirror representing an operation that will be invoked due single entry point in an application. Instances of this
 * mirror are normally acquired by calling various methods on a {@link EntryPointExtensionMirror} instance.
 */
public final class BeanLifecycleMirror extends OperationMirror {

    /** {@return the state at which the operation will be invoked.} */
    public RunState state() {
        return RunState.RUNNING;
    }

    public static void main(ApplicationMirror am) {
        am.operations(BeanLifecycleMirror.class).stream().filter(m -> m.state() == RunState.INITIALIZED).count();
    }
}

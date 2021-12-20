package app.packed.application.entrypoint;

import app.packed.bean.member.BeanOperationMirror;

/**
 * A mirror representing a single entry point in an application. Instances of this mirror are normally acquired by
 * calling various methods on a {@link EntryPointExtensionMirror} instance.
 */
public abstract class EntryPointMirror extends BeanOperationMirror {

    /** {@return the unique id of the entry point within the application.} */
    public abstract int entryPointId();
}
// CliCommandMirror extends EntryPointMirro <---  
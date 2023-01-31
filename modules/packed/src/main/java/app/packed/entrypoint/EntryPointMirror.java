package app.packed.entrypoint;

import app.packed.operation.OperationMirror;

/**
 * A mirror representing a single entry point in an application.
 */
// Name? Operation also has one...
public class EntryPointMirror extends OperationMirror {

    /** The unique id of the entry point within the application. */
    private final int entryPointId;

    EntryPointMirror(int entryPointId) {
        this.entryPointId = entryPointId;
    }

    /** {@return the unique id of the entry point within the application.} */
    public int entryPointId() {
        return entryPointId;
    }

    @Override
    public String toString() {
        return "Entrypoint Main - " + super.target();
    }

}
// CliCommandMirror extends EntryPointMirro <---  

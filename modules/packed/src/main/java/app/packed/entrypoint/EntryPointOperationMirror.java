package app.packed.entrypoint;

import app.packed.operation.OperationMirror;

/**
 * A mirror representing a single entry point in an application.
 * <p>
 * Instances of this mirror are normally acquired by calling various methods on a {@link EntryPointExtensionMirror}
 * instance.
 */
public class EntryPointOperationMirror extends OperationMirror {

    /** The unique id of the entry point within the application. */
    private final int entryPointId;

    EntryPointOperationMirror(int entryPointId) {
        this.entryPointId = entryPointId;
    }

    /** {@return the unique id of the entry point within the application.} */
    public int entryPointId() {
        return entryPointId;
    }
}
// CliCommandMirror extends EntryPointMirro <---  

//Lad os proeve bare at have en enkelt
//Maybe final?

//EntryPoint is not a lifecycle
////Det giver ogsaa mening hvis der er flere entry points
////Det bliver jo foerst besluttet paa Runtime
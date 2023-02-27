package app.packed.lifetime;

import app.packed.operation.OperationMirror;

/**
 * A mirror representing a single entry point in an application.
 */
// Name? Operation also has one... I guess we take it from there then
//CliCommandMirror extends EntryPointMirro <---
public class EntryPointMirror extends OperationMirror {

    /** The unique id of the entry point within the application. */
    // We need operationLocal...
    private final int entryPointId = 0;

    /** {@return the unique id of the entry point within the application.} */
    public int entryPointId() {
        return entryPointId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Entrypoint Main - " + super.target();
    }
}
//Q) CliArgumentMirror???
//        CliArgumentMirror extends EntryPointMirror
//        or EntryPointMirror->CliArgument
//        I think EntryPointMirror is best, otherwise we have
//        EntryPointMirror->CliArgument og CliArgument->EntryPointMirror

package app.packed.lifetime;

import app.packed.operation.OperationMirror;

/**
 * A mirror representing a main entry point.
 * <p>
 * Extensions can subclass this class.
 */
//
public class MainOperationMirror extends OperationMirror {

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

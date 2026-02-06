package app.packed.application;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;

/**
 * A mirror representing an application entry point using {@link Main}.
 *
 * @see Main
 * @see MainOperationConfiguration
 *
 */
public final class MainOperationMirror extends OperationMirror {

    /**
     * @param handle
     */
    public MainOperationMirror(OperationHandle<?> handle) {
        super(handle);
    }

    // The returned class might differ from the return type on the operation target.
    public Class<?> resultType() {
        return void.class;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Entrypoint Main - " + super.target();
    }
}

//We do not have a specific EntryPointOperationMirror
//Because for some operations it might be be decided if it is an entry point or not at build time.
//Would be nice with an example, but trust it now

//Q) CliArgumentMirror???
//        CliArgumentMirror extends EntryPointMirror
//        or EntryPointMirror->CliArgument
//        I think EntryPointMirror is best, otherwise we have
//        EntryPointMirror->CliArgument og CliArgument->EntryPointMirror

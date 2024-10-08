package app.packed.lifetime;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;

/**
 * A mirror representing an entry point using {@link Main}.
 * <p>
 * Extensions can subclass this class. // Why?
 */
// We do not have a specific EntryPointOperationMirror
// Because for some operations it might be be decided if it is an entry point or not at build time.
public class MainOperationMirror extends OperationMirror {

    /**
     * @param handle
     */
    public MainOperationMirror(OperationHandle<?> handle) {
        super(handle);
    }

    /** {@return the lifetime this operation is an entry point for.} */
    public LifetimeMirror lifetime() {
        return entryPointIn().get(); // Should always be present.
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
//Q) CliArgumentMirror???
//        CliArgumentMirror extends EntryPointMirror
//        or EntryPointMirror->CliArgument
//        I think EntryPointMirror is best, otherwise we have
//        EntryPointMirror->CliArgument og CliArgument->EntryPointMirror

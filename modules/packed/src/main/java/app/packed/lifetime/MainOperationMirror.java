package app.packed.lifetime;

import app.packed.operation.OperationMirror;

/**
 * A mirror representing an entry point using {@link Main}.
 * <p>
 * Extensions can subclass this class. // Why?
 */
public class MainOperationMirror extends OperationMirror {

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Entrypoint Main - " + super.target();
    }

    /** {@return the lifetime this operation is an entry point for.} */
    public LifetimeMirror lifetime() {
        return entryPointIn().get(); // Should always be present.
    }

// The returned class might differ from the return type on the operation target.
    public Class<?> resultType() {
        return void.class;
    }
}
//Q) CliArgumentMirror???
//        CliArgumentMirror extends EntryPointMirror
//        or EntryPointMirror->CliArgument
//        I think EntryPointMirror is best, otherwise we have
//        EntryPointMirror->CliArgument og CliArgument->EntryPointMirror

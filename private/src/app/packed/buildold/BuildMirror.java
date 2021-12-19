package app.packed.buildold;

import app.packed.application.ApplicationMirror;

/**
 * A mirror of a single build.
 */
public interface BuildMirror {

    /** {@return the root application of the build}. */
    ApplicationMirror application();

    default TreeView<ApplicationMirror> applications() {
        throw new UnsupportedOperationException();
    }

    // Mirror selection for the whole build

    interface TreeView<T> {}
}

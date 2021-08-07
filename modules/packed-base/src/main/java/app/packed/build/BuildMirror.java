package app.packed.build;

import app.packed.application.ApplicationMirror;

/**
 * A mirror of a single build.
 */
public interface BuildMirror {

    /** {@return the root application of the build}. */
    ApplicationMirror application();

    /** {@return the kind of build.} */
    BuildKind kind();
}

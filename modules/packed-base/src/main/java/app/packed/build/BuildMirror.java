package app.packed.build;

import app.packed.application.ApplicationMirror;

public interface BuildMirror {

    /** {@return the root application of the build}. */
    ApplicationMirror application();
}

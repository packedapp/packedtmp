package app.packed.build;

import app.packed.application.ApplicationMirror;

/**
 * A mirror of a single build.
 */
public interface BuildMirror {

    /** {@return the root application of the build}. */
    ApplicationMirror application();

    // Ideen er vi har en masse metoder her der kan itererer og alle
}
package app.packed.application.entrypoint;

import app.packed.application.ApplicationMirror;

// Spoergsmaalet er om hver extension boer have hver deres mirrors
public interface EntryPointMirror {

    /** {@return the application the entry point belongs to.} */
    ApplicationMirror application();
    // App
}

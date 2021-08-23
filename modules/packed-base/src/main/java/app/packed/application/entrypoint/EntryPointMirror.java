package app.packed.application.entrypoint;

import app.packed.application.ApplicationMirror;
import app.packed.component.ComponentMirror;

// Spoergsmaalet er om hver extension boer have hver deres mirrors

// ApplicationEntryPointMirror... RequestBeanEntryPointMirror
// Maaske er det bare fixtures???

// Hvordan kan man se forskel på om den booter applikationen eller bean'en componenent

public interface EntryPointMirror {

    /** {@return the application the entry point is a part of.} */
    ApplicationMirror application();

    /** {@return the component the entry point is a part of.} */
    ComponentMirror component();

    // MethodFunctionOrField apipoint(); fixture???
}


package app.packed.application;

import app.packed.bean.member.operation.RuntimeOperationMirror;
import app.packed.component.ComponentMirror;

// Spoergsmaalet er om hver extension boer have hver deres mirrors

// ApplicationEntryPointMirror... RequestBeanEntryPointMirror
// Maaske er det bare fixtures???

// Hvordan kan man se forskel på om den booter applikationen eller bean'en componenent

public interface EntryPointMirror extends RuntimeOperationMirror {

    /** {@return the component the entry point is a part of.} */
    ComponentMirror component();

    // MethodFunctionOrField apipoint(); fixture???
}

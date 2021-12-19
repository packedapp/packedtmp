package app.packed.application.entrypoint;

import app.packed.bean.member.BeanOperationMirror;

// Spoergsmaalet er om hver extension boer have hver deres mirrors

// ApplicationEntryPointMirror... RequestBeanEntryPointMirror
// Maaske er det bare fixtures???

// Hvordan kan man se forskel på om den booter applikationen eller bean'en componenent

/**
 * A mirror representing a single entry point in an application. Instances of this mirror are normally acquired from
 * {@link EntryPointExtensionMirror}.
 */
public abstract class EntryPointMirror extends BeanOperationMirror {

    /** {@return the unique id of the entry point within the application.} */
    public abstract int id();

}
// ting vi faar fra BeanOperationen
// has result / or is void


/// Hmm skal den vaere abstract klasse????
/// Taenker vi jo gerne
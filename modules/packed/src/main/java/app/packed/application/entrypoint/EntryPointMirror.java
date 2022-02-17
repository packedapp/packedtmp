package app.packed.application.entrypoint;

import app.packed.bean.mirror.BeanOperationMirror;

/**
 * A mirror representing a single entry point in an application. Instances of this mirror are normally acquired by
 * calling various methods on a {@link EntryPointExtensionMirror} instance.
 */
public class EntryPointMirror extends BeanOperationMirror {

    /** {@return the unique id of the entry point within the application.} */
    public final int entryPointId() {
        return 0;
    }
}
// CliCommandMirror extends EntryPointMirro <---  


//Lad os proeve bare at have en enkelt
//Maybe final?

//EntryPoint is not a lifecycle
////Det giver ogsaa mening hvis der er flere entry points
////Det bliver jo foerst besluttet paa Runtime

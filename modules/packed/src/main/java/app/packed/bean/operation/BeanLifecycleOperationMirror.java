package app.packed.bean.operation;

import app.packed.bean.member.BeanOperationMirror;
import app.packed.lifecycle.RunState;

// Lad os proeve bare at have en enkelt
// Maybe final?

// EntryPoint is not a lifecycle
//// Det giver ogsaa mening hvis der er flere entry points
//// Det bliver jo foerst besluttet paa Runtime
public final class BeanLifecycleOperationMirror extends BeanOperationMirror {

//    // Paa en eller anden maade maa den bindes til noget...
//    Object boundTo();
//
//    /** {@return the component behind the action.} */
//    ComponentMirror component();
//
//    InterruptPolicy interruptPolicy();
//
//    boolean isAsync();

    public boolean isFactory() {
        // isInitializer
        // Ideen er at constructuren ser anderledes ud
        return false;
    }
    
    public RunState state() {
        return RunState.RUNNING;
    }
}

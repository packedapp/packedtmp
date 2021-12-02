package app.packed.lifecycle.sandbox;

import app.packed.component.ComponentMirror;
import app.packed.lifecycle.RunState;
import app.packed.state.InterruptPolicy;

// Lad os proeve bare at have en enkelt
public interface LifecycleActionMirror {

    // Paa en eller anden maade maa den bindes til noget...
    Object boundTo();

    /** {@return the component behind the action.} */
    ComponentMirror component();

    InterruptPolicy interruptPolicy();

    boolean isAsync();

    RunState state();
}

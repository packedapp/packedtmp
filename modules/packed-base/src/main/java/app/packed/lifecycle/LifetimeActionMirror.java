package app.packed.lifecycle;

import app.packed.component.ComponentMirror;
import app.packed.state.InterruptPolicy;
import app.packed.state.sandbox.InstanceState;

// Lad os proeve bare at have en enkelt
public interface LifetimeActionMirror {

    // Paa en eller anden maade maa den bindes til noget...
    Object boundTo();

    /** {@return the component behind the action.} */
    ComponentMirror component();

    InterruptPolicy interruptPolicy();

    boolean isAsync();

    InstanceState state();
}

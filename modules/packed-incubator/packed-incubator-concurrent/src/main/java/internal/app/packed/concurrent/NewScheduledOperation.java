package internal.app.packed.concurrent;

import internal.app.packed.ValueBased;
import internal.app.packed.extension.ExtensionContext;

/**
*
*/
@ValueBased
public abstract class NewScheduledOperation {
    ScheduleImpl s;

    public NewScheduledOperation(ScheduleImpl s) {
        this.s = s;
    }

    public abstract void invoke(ExtensionContext context);
}

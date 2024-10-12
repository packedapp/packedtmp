package internal.app.packed.concurrent;

import app.packed.extension.ExtensionContext;
import internal.app.packed.ValueBased;

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

package internal.app.packed.concurrent;

import java.lang.invoke.MethodHandle;

/**
*
*/
public record ScheduledOperation(ScheduleImpl s, MethodHandle callMe) {}

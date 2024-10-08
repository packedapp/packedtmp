package internal.app.packed.concurrent;

import java.lang.invoke.MethodHandle;

import internal.app.packed.ValueBased;

/**
*
*/
@ValueBased
public record ScheduledOperation(ScheduleImpl s, MethodHandle callMe) {}

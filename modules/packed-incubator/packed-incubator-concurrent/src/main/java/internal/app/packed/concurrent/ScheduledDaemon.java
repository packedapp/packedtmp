package internal.app.packed.concurrent;

import java.lang.invoke.MethodHandle;

/**
*
*/
public record ScheduledDaemon(boolean useVirtual, MethodHandle callMe) {}

package internal.app.packed.concurrent.daemon;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.ThreadFactory;

/**
*
*/
public record DaemonRuntimeConfiguration(ThreadFactory threadFactory, MethodHandle callMe) {}

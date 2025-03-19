package internal.app.packed.concurrent.daemon;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.ThreadFactory;

/**
*
*/
public record DaemonRuntimeOperationConfiguration(ThreadFactory threadFactory, MethodHandle callMe) {}

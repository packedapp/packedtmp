package internal.app.packed.concurrent.daemon;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.TimeUnit;

import app.packed.concurrent.job.DaemonJobContext;
import app.packed.extension.ExtensionContext;

/**
 * A {@link Runnable} that is used to create the daemon thread using
 * {@link java.util.concurrent.ThreadFactory#newThread(Runnable)}.
 */

// Transition to Types Invokers
//@SuppressWarnings("unused")
//NewScheduledOperation[] newSo = operations(ScheduledOperationHandle.class).map(h -> h.invokerAs(NewScheduledOperation.class, h.s))
//      .toArray(NewScheduledOperation[]::new);


public record DaemonRunner(DaemonRuntimeManager manager, MethodHandle mh) implements Runnable {

    /** {@inheritDoc} */
    @Override
    public void run() {
        DaemonJobContext dc = new PackedDaemonContext();
        ExtensionContext ec = manager.extensionContext;
        for (;;) {
            try {
                mh.invokeExact(ec, dc);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


    /** Implementation of {@link DaemonContext}. */
    public static final class PackedDaemonContext implements DaemonJobContext {

        /** {@inheritDoc} */
        @Override
        public boolean isShutdown() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public void awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
            System.out.println("AWAIT");
        }

        /** {@inheritDoc} */
        @Override
        public void awaitShutdown() throws InterruptedException {
            System.out.println("AWAIT");
        }
    }
}

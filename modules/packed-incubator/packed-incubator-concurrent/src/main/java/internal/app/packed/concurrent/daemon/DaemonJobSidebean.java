package internal.app.packed.concurrent.daemon;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import app.packed.bean.SidehandleBinding;
import app.packed.bean.SidehandleBinding.Kind;
import app.packed.bean.lifecycle.Start;
import app.packed.bean.lifecycle.Stop;
import app.packed.concurrent.DaemonJobContext;

public final class DaemonJobSidebean implements DaemonJobContext {

    /** A latch that is counted down when the daemon is requested to stop. */
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    /** Tracks if the stop lifecycle has been initiated. */
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    private final ThreadFactory factory;
    private final DaemonOperationInvoker invoker;
    private final DaemonJobRuntimeManager manager;

    /** Configuration: should we interrupt the thread when stopping? */
    private final boolean interruptOnShutdown;

    private volatile Thread thread;

    public DaemonJobSidebean(@SidehandleBinding(Kind.HANDLE_CONSTANT) ThreadFactory factory,
            @SidehandleBinding(Kind.OPERATION_INVOKER) DaemonOperationInvoker invoker, DaemonJobRuntimeManager manager) {
        this.factory = requireNonNull(factory);
        this.invoker = requireNonNull(invoker);
        this.manager = requireNonNull(manager);
        // This could be passed in via a configuration object/annotation
        this.interruptOnShutdown = true;
    }

    @Override
    public boolean awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
        if (isShutdown()) {
            return false;
        }
        return !shutdownLatch.await(timeout, unit);
    }

    @Override
    public boolean isShutdown() {
        return isShutdown.get();
    }

    @Start
    protected void onStart() {
        thread = factory.newThread(() -> {
            manager.deamons.put(Thread.currentThread(), this);
            try {
                invoker.invoke(this);
            } catch (InterruptedException e) {
                // Expected on shutdown if interruptOnShutdown is true
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                // Production logging instead of e.printStackTrace()
                System.err.println("Daemon thread terminated unexpectedly: " + e.getMessage());
            } finally {
                // Ensure cleanup always happens
                isShutdown.set(true);
                shutdownLatch.countDown();
                manager.deamons.remove(Thread.currentThread());
            }
        });

        if (thread == null) {
            throw new IllegalStateException("ThreadFactory returned null");
        }
        thread.start();
    }

    @Stop
    protected void onStop() {
        // Atomic transition to shutdown state
        if (isShutdown.compareAndSet(false, true)) {
            // 1. Trigger the latch so awaitShutdown() returns false immediately
            shutdownLatch.countDown();

            // 2. Interrupt the thread if the user wants to break out of blocking I/O
            Thread t = thread;
            if (t != null && interruptOnShutdown) {
                t.interrupt();
            }
        }
    }

    interface DaemonOperationInvoker {
        void invoke(DaemonJobContext context) throws Throwable;
    }
}
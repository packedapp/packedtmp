package app.packed.concurrent.cron.impl;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class VirtualCronScheduler {

    public static <T> ScheduledFuture<T> schedule(String cronExpression, ZoneId zoneId, Callable<T> callable) throws Exception {
        CronExpression cron = CronExpression.of(cronExpression);
        CronScheduledFuture<T> future = new CronScheduledFuture<>(cron, callable, zoneId);
        future.start();
        return future;
    }

    private static class CronScheduledFuture<T> implements ScheduledFuture<T> {
        private final CronExpression cronExpression;
        private final Callable<T> callable;
        private final ZoneId zoneId;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition doneCondition = lock.newCondition();

        private volatile boolean cancelled = false;
        private volatile Thread runnerThread; // The virtual thread that runs the task
        private volatile T result;
        private volatile Exception exception;
        private volatile long nextExecutionTime; // in milliseconds since epoch
        private volatile boolean resultAvailable = false;

        public CronScheduledFuture(CronExpression cronExpression, Callable<T> callable, ZoneId zoneId) {
            this.cronExpression = cronExpression;
            this.callable = callable;
            this.zoneId = zoneId;
        }

        public void start() {
            // Create a virtual thread
            runnerThread = Thread.ofVirtual().start(() -> {
                try {
                    while (!cancelled) {
                        ZonedDateTime now = ZonedDateTime.now(zoneId);
                        ZonedDateTime nextTime = cronExpression.nextInZone(now);
                        if (nextTime == null) {
                            break; // No more scheduled times
                        }
                        nextExecutionTime = nextTime.toInstant().toEpochMilli();

                        // Wait until the next execution time
                        long sleepUntil = nextExecutionTime;
                        while (!cancelled) {
                            long nowMillis = System.currentTimeMillis();
                            long timeToWait = sleepUntil - nowMillis;
                            if (timeToWait <= 0) {
                                break; // Time to execute
                            }
                            LockSupport.parkUntil(sleepUntil);
                            if (Thread.interrupted() && cancelled) {
                                break;
                            }
                        }

                        if (cancelled) {
                            break;
                        }

                        lock.lock();
                        try {
                            result = callable.call();
                            exception = null;
                            resultAvailable = true;
                            doneCondition.signalAll();
                        } catch (Exception e) {
                            exception = e;
                            resultAvailable = true;
                            doneCondition.signalAll();
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (Exception e) {
                    exception = e;
                }
            });
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            if (runnerThread != null) {
                runnerThread.interrupt();
                LockSupport.unpark(runnerThread);
            }
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            lock.lock();
            try {
                return cancelled;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            lock.lock();
            try {
                while (!resultAvailable && !cancelled) {
                    doneCondition.await();
                }
                if (cancelled) {
                    throw new CancellationException("Task was cancelled.");
                }
                if (exception != null) {
                    throw new ExecutionException(exception);
                }
                T res = result;
                resultAvailable = false; // Reset for the next execution
                return res;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long nanosTimeout = unit.toNanos(timeout);
            lock.lock();
            try {
                while (!resultAvailable && !cancelled) {
                    if (nanosTimeout <= 0L) {
                        throw new TimeoutException();
                    }
                    nanosTimeout = doneCondition.awaitNanos(nanosTimeout);
                }
                if (cancelled) {
                    throw new CancellationException("Task was cancelled.");
                }
                if (exception != null) {
                    throw new ExecutionException(exception);
                }
                T res = result;
                resultAvailable = false; // Reset for the next execution
                return res;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delayMillis = nextExecutionTime - System.currentTimeMillis();
            return unit.convert(delayMillis, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            long otherDelay = o.getDelay(TimeUnit.MILLISECONDS);
            long thisDelay = getDelay(TimeUnit.MILLISECONDS);
            return Long.compare(thisDelay, otherDelay);
        }
    }
}

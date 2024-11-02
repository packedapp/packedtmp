package internal.app.packed.concurrent.cron;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CronScheduler {

    public static <T> ScheduledFuture<T> schedule(String cronExpression, Callable<T> callable, ScheduledExecutorService executorService) throws Exception {
        CronExpression cron = CronExpression.of(cronExpression);
        CronScheduledFuture<T> future = new CronScheduledFuture<>(cron, callable, executorService);
        future.scheduleNext();
        return future;
    }

    private static class CronScheduledFuture<T> implements ScheduledFuture<T> {
        private final CronExpression cronExpression;
        private final Callable<T> callable;
        private final ScheduledExecutorService executorService;

        private volatile ScheduledFuture<?> currentFuture;
        private volatile boolean cancelled = false;
        private volatile T lastResult = null;
        private volatile Exception lastException = null;
        private final Object lock = new Object();

        public CronScheduledFuture(CronExpression cronExpression, Callable<T> callable, ScheduledExecutorService executorService) {
            this.cronExpression = cronExpression;
            this.callable = callable;
            this.executorService = executorService;
        }

        public void scheduleNext() {
            if (cancelled) {
                return;
            }
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime nextValidTime = cronExpression.nextInZone(now);
            if (nextValidTime == null) {
                // No more executions
                return;
            }
            long delay = Duration.between(now, nextValidTime).toMillis();
            synchronized (lock) {
                currentFuture = executorService.schedule(() -> {
                    try {
                        lastResult = callable.call();
                    } catch (Exception e) {
                        lastException = e;
                    }
                    scheduleNext();
                }, delay, TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            synchronized (lock) {
                return currentFuture != null && currentFuture.cancel(mayInterruptIfRunning);
            }
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return cancelled || (currentFuture != null && currentFuture.isDone());
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            // Wait until the task is cancelled
            while (!isDone()) {
                Thread.sleep(100);
            }
            if (lastException != null) {
                throw new ExecutionException(lastException);
            }
            return lastResult;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long millisTimeout = unit.toMillis(timeout);
            long waited = 0;
            while (!isDone() && waited < millisTimeout) {
                Thread.sleep(100);
                waited += 100;
            }
            if (!isDone()) {
                throw new TimeoutException();
            }
            if (lastException != null) {
                throw new ExecutionException(lastException);
            }
            return lastResult;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            synchronized (lock) {
                if (currentFuture == null) {
                    return 0;
                }
                return currentFuture.getDelay(unit);
            }
        }

        @Override
        public int compareTo(Delayed o) {
            synchronized (lock) {
                if (currentFuture == null) {
                    return -1;
                }
                return currentFuture.compareTo(o);
            }
        }
    }
}


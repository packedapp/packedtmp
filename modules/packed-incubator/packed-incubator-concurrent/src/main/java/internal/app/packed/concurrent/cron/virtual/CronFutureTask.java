package internal.app.packed.concurrent.cron.virtual;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;

import internal.app.packed.concurrent.cron.CronExpression;

public class CronFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

    /** Sequence number to break ties FIFO */
    private final long sequenceNumber;

    /** The next execution time based on the cron expression */
    private volatile long nextExecutionTime;

    /** The cron expression for scheduling */
    private final CronExpression cronExpression;

    /** The actual task to be re-enqueued */
    RunnableScheduledFuture<V> outerTask = this;

    /** Index into delay queue, to support faster cancellation */
    int heapIndex;

    /** Time zone for scheduling */
    private final ZoneId zoneId;

    /** Indicates if the task has been cancelled */
    private volatile boolean cancelled = false;

    /**
     * Creates a CronFutureTask with the given cron expression and sequence number.
     */
    public CronFutureTask(Runnable runnable, V result, CronExpression cronExpression, ZoneId zoneId, long sequenceNumber) {
        super(runnable, result);
        this.cronExpression = cronExpression;
        this.zoneId = zoneId;
        this.sequenceNumber = sequenceNumber;
        this.nextExecutionTime = computeNextExecutionTime();
    }

    /**
     * Creates a CronFutureTask with the given cron expression and sequence number.
     */
    public CronFutureTask(Callable<V> callable, CronExpression cronExpression, ZoneId zoneId, long sequenceNumber) {
        super(callable);
        this.cronExpression = cronExpression;
        this.zoneId = zoneId;
        this.sequenceNumber = sequenceNumber;
        this.nextExecutionTime = computeNextExecutionTime();
    }

    /**
     * Computes the next execution time based on the cron expression.
     */
    private long computeNextExecutionTime() {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime nextTime = cronExpression.getNextValidTimeAfter(now);
        if (nextTime == null) {
            return -1; // Indicates no more executions
        }
        return nextTime.toInstant().toEpochMilli();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delayMillis = nextExecutionTime - System.currentTimeMillis();
        return unit.convert(delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other == this) // compare zero if same object
            return 0;
        if (other instanceof CronFutureTask) {
            CronFutureTask<?> x = (CronFutureTask<?>) other;
            long diff = nextExecutionTime - x.nextExecutionTime;
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else if (sequenceNumber < x.sequenceNumber)
                return -1;
            else
                return 1;
        }
        long diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
        return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }

    /**
     * Returns {@code true} because this is a periodic (cron-based) action.
     */
    @Override
    public boolean isPeriodic() {
        return true;
    }

    /**
     * Sets the next execution time based on the cron expression.
     */
    private void setNextExecutionTime() {
        this.nextExecutionTime = computeNextExecutionTime();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelled = super.cancel(mayInterruptIfRunning);
        return cancelled;
    }

    /**
     * Overrides FutureTask version so as to reset/requeue based on cron.
     */
    @Override
    public void run() {
        if (cancelled) {
            return;
        }

        if (!isPeriodic()) {
            super.run();
        } else if (super.runAndReset()) {
            setNextExecutionTime();
            reExecutePeriodic(outerTask);
        }
    }

    /**
     * Requeues the periodic task for the next execution.
     */
    void reExecutePeriodic(RunnableScheduledFuture<V> task) {
        if (!cancelled && nextExecutionTime > 0) {
            // Re-enqueue the task in the scheduler's delay queue
            // This requires access to the scheduler's queue
            // For demonstration purposes, we'll assume a method exists:
            // schedulerQueue.add(task);
            // You need to integrate this with your scheduler's queue management
        } else {
            cancel(false);
        }
    }
}

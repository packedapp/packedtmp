package app.packed.concurrent.cron.impl.virtual;
import java.time.ZoneId;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import app.packed.concurrent.cron.impl.CronExpression;
import app.packed.concurrent.cron.impl.CronFutureTask;

public class CronSchedulerExecutor extends ThreadPoolExecutor {

    private final DelayQueue<RunnableScheduledFuture<?>> delayQueue = new DelayQueue<>();

    private final AtomicLong sequenceNumber = new AtomicLong(0);

    public CronSchedulerExecutor(int corePoolSize) {
        super(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
        this.startTaskProcessingThread();
    }

    private void startTaskProcessingThread() {
        Thread taskProcessor = new Thread(() -> {
            while (!isShutdown()) {
                try {
                    RunnableScheduledFuture<?> task = delayQueue.take();
                    if (task.isCancelled()) {
                        continue;
                    }
                    super.execute(task);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        taskProcessor.setDaemon(true);
        taskProcessor.start();
    }

    public <V> ScheduledFuture<V> schedule(Runnable command, V result, CronExpression cronExpression, ZoneId zoneId) {
        long seq = sequenceNumber.getAndIncrement();
        CronFutureTask<V> task = new CronFutureTask<>(command, result, cronExpression, zoneId, seq);
        delayQueue.add(task);
        return task;
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, CronExpression cronExpression, ZoneId zoneId) {
        long seq = sequenceNumber.getAndIncrement();
        CronFutureTask<V> task = new CronFutureTask<>(callable, cronExpression, zoneId, seq);
        delayQueue.add(task);
        return task;
    }
}

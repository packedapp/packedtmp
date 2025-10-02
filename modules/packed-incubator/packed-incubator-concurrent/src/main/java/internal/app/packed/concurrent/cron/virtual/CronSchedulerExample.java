package internal.app.packed.concurrent.cron.virtual;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;

import internal.app.packed.concurrent.cron.CronExpression;

public class CronSchedulerExample {
    public static void main(String[] args) throws Exception {
        @SuppressWarnings("resource")
        CronSchedulerExecutor scheduler = new CronSchedulerExecutor(2);

        String cronExpressionStr = "0/1 * * * *"; // Every minute
        ZoneId zoneId = ZoneId.systemDefault();
        CronExpression cronExpression = CronExpression.of(cronExpressionStr);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            IO.println("Task executed at " + ZonedDateTime.now(zoneId));
        }, null, cronExpression, zoneId);

        // Let the task run for 5 minutes
        Thread.sleep(5 * 60 * 1000);

        // Cancel the task
        future.cancel(false);

        // Shutdown the scheduler
        scheduler.shutdown();
    }
}

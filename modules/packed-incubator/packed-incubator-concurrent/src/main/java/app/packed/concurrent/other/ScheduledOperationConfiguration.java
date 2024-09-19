package app.packed.concurrent.other;

import java.time.ZoneId;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;

// Control error handling...
// Tillader
public class ScheduledOperationConfiguration extends OperationConfiguration {

    /**
     * @param handle
     */
    public ScheduledOperationConfiguration(OperationHandle<?> handle) {
        super(handle);
    }

    public ScheduledOperationConfiguration allowConcurrentJobs() {
        // Default is false...
        // I'm trying to come up with usecases...
        return this;
    }

    /**
     * Schedules
     *
     * @param initialDelay
     *            the time to delay first execution
     * @param period
     *            the period between successive executions
     * @param unit
     *            the time unit of the initialDelay and period parameters
     * @return this configuration
     *
     * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     */
    public ScheduledOperationConfiguration atFixedRate(long initialDelay, long period, TimeUnit unit) {
        return this;
    }

    public ScheduledOperationConfiguration atStartup() {
        return this;
    }

    public ScheduledOperationConfiguration keepSchedulingOnShutdown() {
        // must be explicityly started
        return this;
    }

    public ScheduledOperationConfiguration scheduleFromStarting() {
        // Normally we start scheduling from when the container switches to running
        return this;
    }

    public ScheduledOperationConfiguration startPaused() {
        // Or maybe we have something like startPaused(Trigger)
        // must use schedulingConte xt.resume;

        // Trigger = SomeEvent -> Disk has <10GB space, Christmas eve,
        // Tror ideen er at vi har en TriggerExtension...
        // addTrigger(onEvent....->) IDK
        return this;
    }

    ScheduledOperationConfiguration zoneIt(ZoneId zoneId) {
        // Hmmm... Tror vi fjerener den...
        // i 99.99% af alle tilfaelde er det timezonen i contantaineren der er fin
        // Og i resten maa de kalde wire(..., TimeZoneWirelets.timezone());

        // Only applicable for cron jobs
        // Will override any defaults in TimeExtension...
        // Er vel bare en
        // NestedComponentLocal
        // Ved ikke om der er en nemmere maade at erklare denne
        return this;
    }
}

class ZadIdeas {

    // Fungere ikke for images...
    SchedulingContext createContext() {
        // Ved ikke om vi allerede kan lave en version nu..
        // Behover jo ikke vaere den samme instans vi returnere paa runtime...
        // Virker ikke paa images...
        throw new UnsupportedOperationException();
    }

    // Fungere ikke for images...
    ScheduledFuture<?> future() {
        // creates a future that can be used to cancel the job..
        // Maaske kan vi ogsaa traekke scheduling context ud her???
        throw new UnsupportedOperationException();
    }
}
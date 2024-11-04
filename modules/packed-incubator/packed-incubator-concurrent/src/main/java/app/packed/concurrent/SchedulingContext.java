package app.packed.concurrent;

import app.packed.bean.scanning.BeanTrigger.OnExtensionServiceBeanTrigger;
import app.packed.concurrent.job.JobExtension;
import app.packed.context.Context;

/**
 * A context object available for scheduling.
 */
@OnExtensionServiceBeanTrigger(introspector = JobExtension.MyI.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<JobExtension> {

    /** {@return cancel future invocation of the scheduled operations. In progress operations will be allowed to finish} */
    void cancel();

    /** {@return the number of times the scheduled operation has been called} */
    long invocationCount();

    void pause();

    void resume();
}

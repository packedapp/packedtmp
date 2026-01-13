package app.packed.concurrent.oldscheduling;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.concurrent.JobExtension;
import app.packed.context.Context;

/**
 * A context object available for scheduling.
 */
@AutoService(introspector = SchedulingContextBeanIntrospector.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<JobExtension> {

    /** {@return cancel future invocation of the scheduled operations. In progress operations will be allowed to finish} */
    void cancel();

    /** {@return the number of times the scheduled operation has been called} */
    long invocationCount();

    void pause();

    void resume();
}
final class SchedulingContextBeanIntrospector extends BeanIntrospector<JobExtension> {

    @Override
    public void onAutoService(Key<?> key, OnAutoService service) {
        service.binder().bindContext(SchedulingContext.class);
    }
}
package app.packed.concurrent;

import app.packed.bean.BeanTrigger.BindingClassBeanTrigger;
import app.packed.context.Context;

/**
 * A context object available for scheduling.
 */
@BindingClassBeanTrigger(extension = ThreadExtension.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<ThreadExtension> {

    /** {@return cancel future invocation of the scheduled operations. In progress operations will be allowed to finish} */
    void cancel();

    /** {@return the number of times the scheduled operation has been called} */
    long invocationCount();

    void pause();

    void resume();
}

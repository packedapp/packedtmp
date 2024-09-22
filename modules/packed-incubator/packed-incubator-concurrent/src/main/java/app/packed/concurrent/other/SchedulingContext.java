package app.packed.concurrent.other;

import app.packed.bean.BeanTrigger.BindingClassBeanTrigger;
import app.packed.context.Context;

@BindingClassBeanTrigger(extension = ScheduledJobExtension.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<ScheduledJobExtension> /* extends AttributedElement */ {

    void pause();

    void resume();

    void cancel();
}

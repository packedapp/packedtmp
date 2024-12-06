package app.packed.concurrent.other;

import app.packed.bean.scanning.BeanTrigger.OnExtensionServiceBeanTrigger;
import app.packed.context.Context;

@OnExtensionServiceBeanTrigger(introspector = ScheduledJobExtension.ScheduledJobBeanIntrospector.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<ScheduledJobExtension> /* extends AttributedElement */ {

    void pause();

    void resume();

    void cancel();
}

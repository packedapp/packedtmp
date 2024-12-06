package app.packed.concurrent.annotations;

import app.packed.bean.scanning.BeanTrigger.OnExtensionServiceBeanTrigger;
import app.packed.concurrent.other.ScheduledJobExtension;
import app.packed.context.Context;

@OnExtensionServiceBeanTrigger(introspector = ScheduledJobExtension.ScheduledJobBeanIntrospector.class, requiresContext = ScheduledJobContext.class)
public interface ScheduledJobContext extends Context<ScheduledJobExtension> {

    void pause();

    void resume();

    void cancel();
}

package app.packed.concurrent.annotations;

import app.packed.bean.scanning.BeanTrigger.OnContextServiceVariable;
import app.packed.concurrent.other.ScheduledJobExtension;
import app.packed.context.Context;

@OnContextServiceVariable(introspector = ScheduledJobExtension.ScheduledJobBeanIntrospector.class, requiresContext = ScheduledJobContext.class)
public interface ScheduledJobContext extends Context<ScheduledJobExtension> {

    void pause();

    void resume();

    void cancel();
}

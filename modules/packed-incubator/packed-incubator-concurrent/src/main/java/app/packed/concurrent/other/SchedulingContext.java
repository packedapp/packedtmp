package app.packed.concurrent.other;

import app.packed.bean.scanning.BeanTrigger.AutoInject;
import app.packed.context.Context;

@AutoInject(introspector = ScheduledJobBeanIntrospector.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<ScheduledJobExtension> /* extends AttributedElement */ {

    void pause();

    void resume();

    void cancel();
}

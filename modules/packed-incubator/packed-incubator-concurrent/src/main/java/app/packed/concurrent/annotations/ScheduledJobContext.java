package app.packed.concurrent.annotations;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.bean.scanning.BeanTrigger.AutoInject;
import app.packed.concurrent.other.ScheduledJobExtension;
import app.packed.context.Context;

@AutoInject(introspector = ScheduledJobContextBeanIntrospector.class, requiresContext = ScheduledJobContext.class)
public interface ScheduledJobContext extends Context<ScheduledJobExtension> {

    void pause();

    void resume();

    void cancel();
}

final class ScheduledJobContextBeanIntrospector extends BeanIntrospector<ScheduledJobExtension> {

}
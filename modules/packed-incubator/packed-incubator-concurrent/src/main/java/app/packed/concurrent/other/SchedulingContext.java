package app.packed.concurrent.other;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.concurrent.JobExtension;
import app.packed.context.Context;

@AutoService(introspector = SchedulingContextBeanIntrospector.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<ScheduledJobExtension> /* extends AttributedElement */ {

    void pause();

    void resume();

    void cancel();
}
final class SchedulingContextBeanIntrospector extends BeanIntrospector<JobExtension> {

    @Override
    public void onAutoService(Key<?> key, OnAutoService service) {
        service.binder().bindContext(SchedulingContext.class);
    }
}
package app.packed.concurrent.other;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoInject;
import app.packed.binding.Key;
import app.packed.concurrent.JobExtension;
import app.packed.context.Context;

@AutoInject(introspector = SchedulingContextBeanIntrospector.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<ScheduledJobExtension> /* extends AttributedElement */ {

    void pause();

    void resume();

    void cancel();
}
final class SchedulingContextBeanIntrospector extends BeanIntrospector<JobExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        service.binder().bindContext(SchedulingContext.class);
    }
}